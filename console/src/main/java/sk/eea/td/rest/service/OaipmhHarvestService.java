package sk.eea.td.rest.service;

import ORG.oclc.oai.harvester2.verb.HttpResponseCodeException;
import ORG.oclc.oai.harvester2.verb.ListRecords;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;
import sk.eea.td.rest.model.OaipmhConfigWrapper;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.Authenticator;
import java.net.HttpRetryException;
import java.net.PasswordAuthentication;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class OaipmhHarvestService {

    private static Logger LOG = LoggerFactory.getLogger(OaipmhHarvestService.class);

    private static final AtomicBoolean CANCELLED = new AtomicBoolean();

    @Value("${oaipmh.base.url}")
    private String baseUrl;

    @Value("${oaipmh.max.files.in.directory}")
    private int maxFilesInDirectory;

    @Value("${oaipmh.character.encoding}")
    private String encoding = "UTF-8";

    @Value("${oaipmh.retry}")
    private int retry;

    @Value("${oaipmh.retry.delay}")
    private int retryDelay;

    @Value("${oaipmh.max.object.count}")
    private Integer maxObjectsCount;

    @Value("${oaipmh.authorization.string:@null}")
    private String authorizationString;

    @Value("${oaipmh.username}")
    private String username;

    @Value("${oaipmh.password}")
    private String password;

    @Value("${oaipmh.output.directory}")
    private String outputDirectory;

    /**
     * Harvesting using wrapped config.
     *
     * @param oaipmhConfigWrapper
     */
    public void harvest(OaipmhConfigWrapper oaipmhConfigWrapper) throws NoSuchFieldException, IOException, ParserConfigurationException, SAXException, TransformerException {
        harvest(this.baseUrl, oaipmhConfigWrapper.getFrom(), oaipmhConfigWrapper.getUntil(), oaipmhConfigWrapper.getSet(), oaipmhConfigWrapper.getMetadataPrefix());
    }

    /**
     * Harvesting that starts from resumptionToken
     *
     * @param url
     * @param resumptionToken
     * @throws javax.xml.transform.TransformerException
     * @throws org.xml.sax.SAXException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws java.io.IOException
     * @throws NoSuchFieldException
     */
    public void harvest(String url,
            String resumptionToken) throws NoSuchFieldException, IOException, ParserConfigurationException, SAXException, TransformerException {
        harvest(url, resumptionToken, null, null, null, null);
    }

    /**
     * Harvesting according to selected time interval
     *
     * @param url
     * @param from
     * @param until
     * @param set
     * @param metadataPrefix
     * @return harvestedFiles count and harvested objects count
     * @throws javax.xml.transform.TransformerException
     * @throws org.xml.sax.SAXException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws java.io.IOException
     * @throws NoSuchFieldException
     */
    public HarvestingResult harvest(String url, String from,
            String until, String set, String metadataPrefix) throws NoSuchFieldException, IOException, ParserConfigurationException, SAXException, TransformerException {
        return harvest(url, null, from, until, set, metadataPrefix);
    }

    private HarvestingResult harvest(String url, String resumptionToken, String from,
            String until, String set, String metadataPrefix) throws IOException, ParserConfigurationException, SAXException, TransformerException, NoSuchFieldException {

        ListRecords listRecords;
        int harvestedFiles = 0;
        int filesInDir = 0;
        int objectsCount = 0;
        if (CANCELLED.get())
            return new HarvestingResult(harvestedFiles, objectsCount, "Cancelled.");
        File dir = createSubDir(this.outputDirectory);

        if (this.username != null && this.username.length() > 0 && this.password != null && this.password.length() > 0) {
            Authenticator.setDefault(new CustomAuthenticator(this.username, this.password));
        }

        listRecords = getListRecords(url, resumptionToken, from, until, set, metadataPrefix);
        String msg = null;
        while (listRecords != null && listRecords.getResumptionToken() != null) {
            if (CANCELLED.get())
                return new HarvestingResult(harvestedFiles, objectsCount, "Cancelled.");
            if (filesInDir == this.maxFilesInDirectory) {
                dir = createSubDir(this.outputDirectory);
                filesInDir = 0;
            }

            int length = listRecords.getNodeList("//*[name()='record']").getLength();
            if (length > 0) {
                objectsCount += length;
                harvestedFiles++;

                filesInDir++;
                File file = new File(dir, String.valueOf(System.currentTimeMillis()) + ".xml");
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), this.encoding));
                out.write(listRecords.toString());
                out.close();
                if (this.maxObjectsCount != null && this.maxObjectsCount > 0 && objectsCount >= maxObjectsCount) {
                    msg = "Max. harvested objects count: " + this.maxObjectsCount + " >= " + objectsCount + " Skip harvesting.";
                    LOG.info(msg);
                    break;
                }
            }

            if (CANCELLED.get())
                return new HarvestingResult(harvestedFiles, objectsCount, "Cancelled.");
            resumptionToken = listRecords.getResumptionToken();
            listRecords = getRecordsFromResumptionToken(url, resumptionToken);
        }
        LOG.info("Harvested files: " + harvestedFiles);
        LOG.info("Harvested objects: " + objectsCount);
        return new HarvestingResult(harvestedFiles, objectsCount, msg);
    }

    private ListRecords getRecordsFromResumptionToken(String url, String resumptionToken) throws IOException, ParserConfigurationException, SAXException, TransformerException {
        if (!StringUtils.isBlank(resumptionToken) && url.startsWith("http")) {
            return getListRecords(url, resumptionToken, null, null, null, null);
        }
        return null;
    }

    /**
     * Gets the records from the url, if the http response code isnt ok,
     * retries selected number of times with selected delay. (see constructor)
     *
     * @param url
     * @param resumptionToken
     * @param from
     * @param until
     * @param set
     * @param metadataPrefix
     * @return
     * @throws javax.xml.transform.TransformerException
     * @throws org.xml.sax.SAXException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws java.io.IOException
     */
    private ListRecords getListRecords(String url, String resumptionToken, String from, String until, String set, String metadataPrefix) throws IOException, ParserConfigurationException, SAXException, TransformerException {
        int repeated = 0;
        boolean repeat = true;
        ListRecords records = null;

        while (repeat) {
            if (CANCELLED.get())
                return records;
            repeat = false;
            try {
                if (resumptionToken == null) {
                    records = new ListRecords(url, from, until, set, metadataPrefix, this.authorizationString);
                    LOG.info("Harvesting URL: " + records.getRequestURL());
                } else {
                    records = new ListRecords(url, resumptionToken, this.authorizationString);
                    LOG.info("Harvesting URL: " + records.getRequestURL());
                }
                repeated = 0;
            } catch (HttpResponseCodeException e) {
                if (repeated < this.retry) {
                    repeat = true;
                    LOG.info("Harvesting URL: " + ((records == null) ? null : records.getRequestURL()));
                    LOG.warn("Error http response code " + e.getResponseCode() + " ... going to sleep, repeating request in " + this.retryDelay + " [sec]");
                    sleep();
                } else {
                    throw e;
                }
            } catch (HttpRetryException e) {
                if (repeated < this.retry) {
                    repeat = true;
                    LOG.info("Harvesting URL: " + ((records == null) ? null : records.getRequestURL()));
                    LOG.info("Http retry exception thrown. ... going to sleep, repeating request in " + this.retryDelay + " [sec]");
                    sleep();
                } else {
                    throw e;
                }
            }
            repeated++;
        }
        return records;
    }

    private void sleep() {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < this.retryDelay * 1000) {
            try {
                Thread.sleep(this.retryDelay * 1000);
            } catch (InterruptedException e) {
            }
        }
    }

    private File createSubDir(String dir) throws IOException {
        File subDir = new File(dir, String.valueOf(System.currentTimeMillis()));
        if (!subDir.exists()) {
            FileUtils.forceMkdir(subDir);
        }
        return subDir;
    }

    private static class CustomAuthenticator extends Authenticator {
        private String username;

        private String password;

        private CustomAuthenticator(String username, String password) {
            this.username = username;
            this.password = password;
        }

        // Called when password authorization is needed
        protected PasswordAuthentication getPasswordAuthentication() {
            // Get information about the request
            // String prompt = getRequestingPrompt();
            // String hostname = getRequestingHost();
            // InetAddress ipaddr = getRequestingSite();
            // int port = getRequestingPort();
            // Return the information (a data holder that is used by Authenticator)
            return new PasswordAuthentication(username, password.toCharArray());
        }
    }

    public void stop() {
        CANCELLED.set(true);
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    static class HarvestingResult {

        private int harvestedFiles;

        private int objectCount;

        private String status;

        public HarvestingResult(int harvestedFiles, int objectsCount, String status) {
            this.harvestedFiles = harvestedFiles;
            this.objectCount = objectsCount;
            this.status = status;
        }

        public int getHarvestedFiles() {
            return harvestedFiles;
        }

        public void setHarvestedFiles(int harvestedFiles) {
            this.harvestedFiles = harvestedFiles;
        }

        public int getObjectCount() {
            return objectCount;
        }

        public void setObjectCount(int objectCount) {
            this.objectCount = objectCount;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        @Override public String toString() {
            return "HarvestingResult{" +
                    "harvestedFiles=" + harvestedFiles +
                    ", objectCount=" + objectCount +
                    ", status='" + status + '\'' +
                    '}';
        }
    }
}
