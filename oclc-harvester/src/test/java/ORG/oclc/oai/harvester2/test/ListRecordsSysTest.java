package ORG.oclc.oai.harvester2.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

import org.junit.Assert;
import org.junit.Test;

import ORG.oclc.oai.harvester2.verb.ListRecords;

/**
 * Test pre bezne scenar OAI-PMH harvestingu cez Listrecords.
 */
public class ListRecordsSysTest {
    /*
    	SNG: http://www.webumenia.sk:8080/oai-pmh?verb=ListRecords&metadataPrefix=oai_dc
    	MSNP: http://www.cemuz.sk/oai-pmh?verb=ListRecords&metadataPrefix=oai_dc
    	nejake ceske: 
    	MNS: http://omega.manuscriptorium.com/MnOai.php?verb=ListRecords&metadataPrefix=oai_dc	
     */

    //private static final String url = "http://omega.manuscriptorium.com/MnOai.php";
    //private static final String url = "http://www.webumenia.sk/oai-pmh";
    // "http://www.cemuz.sk/oai-pmh"
    private static final String url = "http://www.webumenia.sk/oai-pmh";
    private static final String from = null;
    private static final String until = null;
    private static final String set = null;
    private static final String metadataPrefix = "oai_dc";
    private static final String output = "target/test/output/test_oaipmh/";
    private static final String ENC = "UTF-8";
    private static final int FILES_IN_DIR = 10000;
    private static final int MAX_FILES = 1;

//    @Test
    public void test() {
        ListRecords listRecords;
        int harvestedFiles = 0;
        int filesInDir = 0;
        File dir = createSubDir(output);
        Authenticator.setDefault(new CustomAuthenticator());
        try {
            listRecords = new ListRecords(url, from, until, set, metadataPrefix);
            Assert.assertNotNull(listRecords);
            while (listRecords != null) {
            	if (MAX_FILES <= harvestedFiles + 1) break;
                if (filesInDir == FILES_IN_DIR) {
                    dir = createSubDir(output);
                    filesInDir = 0;
                }
                harvestedFiles++;
                filesInDir++;
                String fileName = dir + File.separator + String.valueOf(System.currentTimeMillis()) + ".xml";
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), ENC));
                out.write(listRecords.toString());
                out.close();
                String resumptionToken = listRecords.getResumptionToken();
                if (resumptionToken == null || resumptionToken.length() == 0) {
                    listRecords = null;
                } else {
                    if (url.startsWith("http:")) {
                        listRecords = new ListRecords(url, resumptionToken);
                    } else {
                        listRecords = null;
                    }
                }
            }
            System.out.println("Harvested files: " + harvestedFiles);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File createSubDir(String dir) {
        File subDir = new File(dir + String.valueOf(System.currentTimeMillis()));
        if (subDir.exists() == false) {
            subDir.mkdirs();
        }
        return subDir;
    }
    
    private static class CustomAuthenticator extends Authenticator {
        // Called when password authorization is needed
        protected PasswordAuthentication getPasswordAuthentication() {
            // Get information about the request
            // String prompt = getRequestingPrompt();
            // String hostname = getRequestingHost();
            // InetAddress ipaddr = getRequestingSite();
            // int port = getRequestingPort();

            String username = "tendermediagroup";
            String password = "TMGomega";

            // Return the information (a data holder that is used by Authenticator)
            return new PasswordAuthentication(username, password.toCharArray());
        }
    }
}
