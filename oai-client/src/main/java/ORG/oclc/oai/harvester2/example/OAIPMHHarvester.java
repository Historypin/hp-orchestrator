package ORG.oclc.oai.harvester2.example;

import ORG.oclc.oai.harvester2.verb.HttpResponseCodeException;
import ORG.oclc.oai.harvester2.verb.ListRecords;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.Authenticator;
import java.net.HttpRetryException;
import java.net.PasswordAuthentication;

public class OAIPMHHarvester {

    protected static Logger log = LoggerFactory.getLogger(OAIPMHHarvester.class);

    private static final int FILES_IN_DIR = 10000;

    private static final String ENC = "UTF-8";

    private static final int DEFAULT_RETRY = 5;

    private static final int DEFAULT_RETRY_DELAY = 30 * 60; // [sec]

    private  int retry = DEFAULT_RETRY;

    private  int retryDelay = DEFAULT_RETRY_DELAY; // [sec]

    private Integer maxObjectsCount;

    private String authorizationString = null;

    private boolean cancelled = false;

    public OAIPMHHarvester(String retry, String retryDelay) {
        super();
        if (retry != null && retry.trim().matches("\\d+")) {
            this.retry = Integer.parseInt(retry);
        }
        if (retryDelay != null && retryDelay.trim().matches("\\d+")) {
            this.retryDelay = Integer.parseInt(retryDelay);
        }
    }

    public OAIPMHHarvester(int retry, int retryDelay) {
        super();
        this.retry = retry;
        this.retryDelay = retryDelay;
    }

    public OAIPMHHarvester(String retry, String retryDelay, String authorizationString) {
        this(retry, retryDelay);
        this.authorizationString = authorizationString;
    }

    public OAIPMHHarvester() {
        this(0, 0);
    }

    public Integer getMaxObjectsCount() {
        return maxObjectsCount;
    }

    public void setMaxObjectsCount(String maxObjectsCount) {
        if (maxObjectsCount != null && maxObjectsCount.trim().matches("\\d+")) {
            this.maxObjectsCount = Integer.parseInt(maxObjectsCount);
        }
    }

    /**
     * Harvesting that starts from resumptionToken
     *
     * @param url
     * @param username
     * @param password
     * @param output
     * @param resumptionToken
     * @throws javax.xml.transform.TransformerException
     * @throws org.xml.sax.SAXException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws java.io.IOException
     * @throws NoSuchFieldException
     */
    public void harvest(String url, String username, String password, String output,
            String resumptionToken) throws NoSuchFieldException, IOException, ParserConfigurationException, SAXException, TransformerException {
        harvest(url, resumptionToken, username, password, null, null, null, null, output);
    }

    /**
     * Harvesting according to selected time interval
     *
     * @param url
     * @param username
     * @param password
     * @param from
     * @param until
     * @param set
     * @param metadataPrefix
     * @param output
     * @return harvestedFiles count and harvested objects count
     * @throws javax.xml.transform.TransformerException
     * @throws org.xml.sax.SAXException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws java.io.IOException
     * @throws NoSuchFieldException
     */
    public HarvestingResult harvest(String url, String username, String password, String from,
            String until, String set, String metadataPrefix, String output) throws NoSuchFieldException, IOException, ParserConfigurationException, SAXException, TransformerException {
        return harvest(url, null, username, password, from, until, set, metadataPrefix, output);
    }

    private HarvestingResult harvest(String url, String resumptionToken, String username, String password, String from,
            String until, String set, String metadataPrefix, String output) throws IOException, ParserConfigurationException, SAXException, TransformerException, NoSuchFieldException {

        ListRecords listRecords;
        int harvestedFiles = 0;
        int filesInDir = 0;
        int objectsCount = 0;
        if (cancelled)
            return new HarvestingResult(harvestedFiles, objectsCount, "Cancelled.");
        File dir = createSubDir(output);

        if (username != null && username.length() > 0 && password != null && password.length() > 0) {
            Authenticator.setDefault(new CustomAuthenticator(username, password));
        }

        listRecords = getListRecords(url, resumptionToken, from, until, set, metadataPrefix);
        String msg = null;
        while (listRecords != null && listRecords.getResumptionToken() != null) {
            if (cancelled)
                return new HarvestingResult(harvestedFiles, objectsCount, "Cancelled.");
            if (filesInDir == FILES_IN_DIR) {
                dir = createSubDir(output);
                filesInDir = 0;
            }

            int length = listRecords.getNodeList("//*[name()='record']").getLength();
            if (length > 0) {
                objectsCount += length;
                harvestedFiles++;

                filesInDir++;
                File file = new File(dir, String.valueOf(System.currentTimeMillis()) + ".xml");
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), ENC));
                out.write(listRecords.toString());
                out.close();
                if (maxObjectsCount != null && maxObjectsCount.intValue() > 0 && objectsCount >= maxObjectsCount) {
                    msg = "Max. harvested objects count: " + maxObjectsCount.intValue() + " >= " + objectsCount + " Skip harvesting.";
                    log.info(msg);
                    break;
                }
            }

            if (cancelled)
                return new HarvestingResult(harvestedFiles, objectsCount, "Cancelled.");
            resumptionToken = listRecords.getResumptionToken();
            listRecords = getRecordsFromResumptionToken(url, resumptionToken);
        }
        log.info("Harvested files: " + harvestedFiles);
        log.info("Harvested objects: " + objectsCount);
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
            if (cancelled)
                return records;
            repeat = false;
            try {
                if (resumptionToken == null) {
                    records = new ListRecords(url, from, until, set, metadataPrefix, authorizationString);
                    log.info("Harvesting URL: " + records.getRequestURL());
                } else {
                    records = new ListRecords(url, resumptionToken, authorizationString);
                    log.info("Harvesting URL: " + records.getRequestURL());
                }
                repeated = 0;
            } catch (HttpResponseCodeException e) {
                if (repeated < retry) {
                    repeat = true;
                    log.info("Harvesting URL: " + records.getRequestURL());
                    log.warn("Error http response code " + e.getResponseCode() + " ... going to sleep, repeating request in " + retryDelay + " [sec]");
                    sleep();
                } else {
                    throw e;
                }
            } catch (HttpRetryException e) {
                if (repeated < retry) {
                    repeat = true;
                    log.info("Harvesting URL: " + records.getRequestURL());
                    log.info("Http retry exception thrown. ... going to sleep, repeating request in " + retryDelay + " [sec]");
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
        while (System.currentTimeMillis() - start < retryDelay * 1000) {
            try {
                Thread.sleep(retryDelay * 1000);
            } catch (InterruptedException e) {
            }
        }
    }

    private File createSubDir(String dir) throws IOException {
        File subDir = new File(dir, String.valueOf(System.currentTimeMillis()));
        if (subDir.exists() == false) {
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
        this.cancelled = true;
    }

    public static void main(String[] args) {
        OAIPMHHarvester harvester = new OAIPMHHarvester();
        try {
            HarvestingResult result = harvester.harvest("http://oai.europeana.eu/oaicat/OAIHandler", "admin", "eur0peana0ai", "2015-07-15T19:12:06Z", "2016-02-11T10:00:00Z", "07101_Ag_SK_EuropeanASNG", "edm", "/tmp/test");
            System.out.println(result);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }
}
