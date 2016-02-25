/**
 Copyright 2006 OCLC, Online Computer Library Center
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package ORG.oclc.oai.harvester2.verb;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpRetryException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.xpath.XPathAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * HarvesterVerb is the parent class for each of the OAI verbs.
 * 
 * @author Jefffrey A. Young, OCLC Online Computer Library Center
 */
public abstract class HarvesterVerb {
	private static Logger logger = LoggerFactory.getLogger(HarvesterVerb.class);
	private static Logger logDump = LoggerFactory.getLogger("OAI-Dump");
	// FIXME global option - true=fix illegal utf8 characters in harvested data
	private static boolean fixinput = "true".equals(System.getProperty(
			"harvester2.fixinput", "false"));

	/* Primary OAI namespaces */
	public static final String SCHEMA_LOCATION_V2_0 = "http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd";
	public static final String SCHEMA_LOCATION_V1_1_GET_RECORD = "http://www.openarchives.org/OAI/1.1/OAI_GetRecord http://www.openarchives.org/OAI/1.1/OAI_GetRecord.xsd";
	public static final String SCHEMA_LOCATION_V1_1_IDENTIFY = "http://www.openarchives.org/OAI/1.1/OAI_Identify http://www.openarchives.org/OAI/1.1/OAI_Identify.xsd";
	public static final String SCHEMA_LOCATION_V1_1_LIST_IDENTIFIERS = "http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers.xsd";
	public static final String SCHEMA_LOCATION_V1_1_LIST_METADATA_FORMATS = "http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats.xsd";
	public static final String SCHEMA_LOCATION_V1_1_LIST_RECORDS = "http://www.openarchives.org/OAI/1.1/OAI_ListRecords http://www.openarchives.org/OAI/1.1/OAI_ListRecords.xsd";
	public static final String SCHEMA_LOCATION_V1_1_LIST_SETS = "http://www.openarchives.org/OAI/1.1/OAI_ListSets http://www.openarchives.org/OAI/1.1/OAI_ListSets.xsd";
	public static final String CONTENT_TYPE_HTML = "text/html";
	private Document doc = null;
	private String schemaLocation = null;
	private String requestURL = null;
	private String authorizationString = null;
	private static HashMap builderMap = new HashMap();
	private static Element namespaceElement = null;
	private static DocumentBuilderFactory factory = null;

	private static Transformer idTransformer = null;
	static {
		try {
			/* create transformer */
			TransformerFactory xformFactory = TransformerFactory.newInstance();
			try {
				idTransformer = xformFactory.newTransformer();
				idTransformer.setOutputProperty(
						OutputKeys.OMIT_XML_DECLARATION, "yes");
			} catch (TransformerException e) {
				e.printStackTrace();
			}

			/* Load DOM Document */
			factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			Thread t = Thread.currentThread();
			DocumentBuilder builder = factory.newDocumentBuilder();
			builderMap.put(t, builder);

			DOMImplementation impl = builder.getDOMImplementation();
			Document namespaceHolder = impl.createDocument(
					"http://www.oclc.org/research/software/oai/harvester",
					"harvester:namespaceHolder", null);
			namespaceElement = namespaceHolder.getDocumentElement();
			namespaceElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
					"xmlns:harvester",
					"http://www.oclc.org/research/software/oai/harvester");
			namespaceElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
					"xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			namespaceElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
					"xmlns:oai20", "http://www.openarchives.org/OAI/2.0/");
			namespaceElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
					"xmlns:oai11_GetRecord",
					"http://www.openarchives.org/OAI/1.1/OAI_GetRecord");
			namespaceElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
					"xmlns:oai11_Identify",
					"http://www.openarchives.org/OAI/1.1/OAI_Identify");
			namespaceElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
					"xmlns:oai11_ListIdentifiers",
					"http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers");
			namespaceElement
					.setAttributeNS("http://www.w3.org/2000/xmlns/",
							"xmlns:oai11_ListMetadataFormats",
							"http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats");
			namespaceElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
					"xmlns:oai11_ListRecords",
					"http://www.openarchives.org/OAI/1.1/OAI_ListRecords");
			namespaceElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
					"xmlns:oai11_ListSets",
					"http://www.openarchives.org/OAI/1.1/OAI_ListSets");
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the OAI response as a DOM object
	 * 
	 * @return the DOM for the OAI response
	 */
	public Document getDocument() {
		return doc;
	}

	/**
	 * Get the xsi:schemaLocation for the OAI response
	 * 
	 * @return the xsi:schemaLocation value
	 */
	public String getSchemaLocation() {
		return schemaLocation;
	}

	/**
	 * Get the OAI errors
	 * 
	 * @return a NodeList of /oai:OAI-PMH/oai:error elements
	 * @throws TransformerException
	 */
	public NodeList getErrors() throws TransformerException {
		if (SCHEMA_LOCATION_V2_0.equals(getSchemaLocation())) {
			return getNodeList("/oai20:OAI-PMH/oai20:error");
		} else {
			return null;
		}
	}

	/**
	 * Get the OAI request URL for this response
	 * 
	 * @return the OAI request URL as a String
	 */
	public String getRequestURL() {
		return requestURL;
	}

	/**
	 * Mock object creator (for unit testing purposes)
	 */
	public HarvesterVerb() {
	}

	/**
	 * Performs the OAI request
	 * 
	 * @param requestURL
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws TransformerException
	 */
	public HarvesterVerb(String requestURL) throws IOException,
			ParserConfigurationException, SAXException, TransformerException {
		harvest(requestURL);
	}

	/**
	 * Performs the OAI request
	 * 
	 * @param requestURL
	 * @param authorizationString not mandatory, can be null
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws TransformerException
	 */
	public HarvesterVerb(String requestURL, String authorizationString) throws IOException,
			ParserConfigurationException, SAXException, TransformerException {
		this.authorizationString = authorizationString;
		harvest(requestURL);
	}
	
	/**
	 * Preforms the OAI request
	 * 
	 * @param requestURL
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws TransformerException
	 */
	public void harvest(String requestURL) throws IOException,
			ParserConfigurationException, SAXException, TransformerException {
		this.requestURL = requestURL;
		logger.debug("requestURL=" + requestURL);
		InputStream in = null;
		URL url = new URL(requestURL);
		URLConnection con = null;
		int responseCode = 0;
		do {
			con = url.openConnection();
			
			//authorization string
			if (!StringUtils.isBlank(authorizationString)) {
				if (requestURL.startsWith("https")) 
					logger.debug("Using javax.net.ssl.trustStore=" + System.getProperty("javax.net.ssl.trustStore"));
				con.setRequestProperty("Authorization", "Basic " + authorizationString);
			}
			
			if (con instanceof HttpURLConnection) {
				con.setRequestProperty("User-Agent", "OAIHarvester/2.0");
				con.setRequestProperty("Accept-Encoding",
						"compress, gzip, identify");
			}
			try {
				if (con instanceof HttpURLConnection) {
					responseCode = ((HttpURLConnection) con).getResponseCode();
				}
				logger.debug("responseCode=" + responseCode);
			} catch (FileNotFoundException e) {
				// assume it's a 503 response
				logger.info(requestURL, e);
				responseCode = HttpURLConnection.HTTP_UNAVAILABLE;
			}

			if (responseCode == HttpURLConnection.HTTP_UNAVAILABLE) {
				long retrySeconds = con.getHeaderFieldInt("Retry-After", -1);
				if (retrySeconds == -1) {
					long now = (new Date()).getTime();
					long retryDate = con.getHeaderFieldDate("Retry-After", now);
					retrySeconds = retryDate - now;
				}
				if (retrySeconds == 0) { // Apparently, it's a bad URL
					throw new FileNotFoundException("Bad URL?");
				}
				System.err.println("Server response: Retry-After="
						+ retrySeconds);
				if (retrySeconds > 0) {
					try {
						Thread.sleep(retrySeconds * 1000);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
			} else if (responseCode != 200) {
				if (con instanceof HttpURLConnection) {
					throw new HttpResponseCodeException(responseCode, getHttpErrorMessage(con));
				}
			}
		
		} while (responseCode == HttpURLConnection.HTTP_UNAVAILABLE);
		
		in = getResponseInputStream(con, con.getInputStream());

		// rse: preprocess input file first - fix UTF-8 and/or XML characters
		File tmpfile = null;
		if (fixinput) {
			tmpfile = File.createTempFile("failed-harvest.", ".xml");
			try {
				BufferedReader inr = new BufferedReader(new InputStreamReader(
						in, "UTF-8"));
				BufferedWriter outr = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(tmpfile),
								"UTF-8"));
				String line = null;
				while ((line = inr.readLine()) != null) {
					// workaround na chybne xml chars v SNK
					final String oldLine = line;
					line = line.replaceAll(
							"[\\x00-\\x08\\x0b\\x0c\\x0e-\\x1f]", "?");
					if (!oldLine.equals(line)) {
						logger.warn("Invalid xml chars (fixed): '" + oldLine
								+ "'");
					}
					outr.write(line);
					outr.newLine();
				}
				inr.close();
				outr.close();
				in = new BufferedInputStream(new FileInputStream(tmpfile));
			} catch (Exception ex) {
				logger.error("can't fix input xml - copy stream error", ex);
				throw new IOException("can't fix input xml - copy stream error");
			}
		}

		InputSource data = new InputSource(in);

		Thread t = Thread.currentThread();
		DocumentBuilder builder = (DocumentBuilder) builderMap.get(t);
		if (builder == null) {
			builder = factory.newDocumentBuilder();
			builderMap.put(t, builder);
		}
		try{		
		    doc = builder.parse(data);
		}catch (Exception e){
		    if(logDump.isTraceEnabled()){
		        File dump = File.createTempFile("oai_dump", "xml");
		        BufferedWriter writer = Files.newBufferedWriter(dump.toPath());
		        Files.copy(in, dump.toPath());
		        logDump.error("Problem parsing file. Request stored in: "+dump.getAbsolutePath());
		    }
		   throw new HttpRetryException("Unparseable response", responseCode);
		}

		StringTokenizer tokenizer = new StringTokenizer(
				getSingleString("/*/@xsi:schemaLocation"), " ");
		StringBuffer sb = new StringBuffer();
		while (tokenizer.hasMoreTokens()) {
			if (sb.length() > 0)
				sb.append(" ");
			sb.append(tokenizer.nextToken());
		}
		this.schemaLocation = sb.toString();
		
		if(schemaLocation.toString().isEmpty()){
		    String namespace = doc.getFirstChild().getNamespaceURI();
            for (String schemaLoc : Arrays.asList(new String[]{SCHEMA_LOCATION_V1_1_GET_RECORD,
                SCHEMA_LOCATION_V1_1_IDENTIFY, SCHEMA_LOCATION_V1_1_LIST_IDENTIFIERS,
                SCHEMA_LOCATION_V1_1_LIST_METADATA_FORMATS, SCHEMA_LOCATION_V1_1_LIST_RECORDS,
                SCHEMA_LOCATION_V1_1_LIST_SETS, SCHEMA_LOCATION_V2_0})) {
                if(schemaLoc.startsWith(namespace)){
                    schemaLocation = schemaLoc;
                    break;
                }
		    }
		}
		    
		
		NodeList errorMessages = getNodeList("/oai20:OAI-PMH/oai20:error");
        if (errorMessages.getLength() > 0 ){
            StringBuilder message = new StringBuilder();
            for(int i=0;i < errorMessages.getLength(); i++){
                Node node = errorMessages.item(i);
                String errorMessage = node.getChildNodes().item(0).getNodeValue();
                NamedNodeMap nodeAttributes = node.getAttributes();
                Node errorCodeNode = nodeAttributes.getNamedItemNS("http://www.openarchives.org/OAI/2.0/","code");
                if (errorCodeNode == null) {
                	errorCodeNode = nodeAttributes.getNamedItem("code");
				}
                
                if ("noRecordsMatch".equals(errorCodeNode.getNodeValue())) {
                	// if the returned list is empty
                	logger.info(errorMessage);
					continue;
				}
                
                message.append(";").append(errorCodeNode == null ? null : errorCodeNode.getNodeValue());
                message.append(": ").append(errorMessage);
            }
            if(message.length() > 0){
                throw new IOException(message.substring(1));
            }
        }
		if (tmpfile != null) {
			tmpfile.delete();
		}
	}
	
	public String getHttpErrorMessage(URLConnection con) throws IOException {
		InputStream in = getResponseInputStream(con, ((HttpURLConnection) con).getErrorStream());
		String message = IOUtils.toString(in);
		in.close();
		
		if (con.getContentType().startsWith(CONTENT_TYPE_HTML)) {
			logger.error(message);
			// status line like: HTTP/1.1 504 Gateway Timeout
			return con.getHeaderField(0);
		}
		
		return message;
	}

	private InputStream getResponseInputStream(URLConnection con, InputStream inputStream) throws IOException {
	    String contentEncoding = con.getHeaderField("Content-Encoding");
        logger.debug("contentEncoding=" + contentEncoding);
        if ("compress".equals(contentEncoding)) {
            ZipInputStream zis = new ZipInputStream(inputStream);
            zis.getNextEntry();
            return zis;
        } else if ("gzip".equals(contentEncoding)) {
            return new GZIPInputStream(inputStream);
        } else if ("deflate".equals(contentEncoding)) {
            return new InflaterInputStream(inputStream);
        } else {
            return inputStream;
        }
    }

    /**
	 * Get the String value for the given XPath location in the response DOM
	 * 
	 * @param xpath
	 * @return a String containing the value of the XPath location.
	 * @throws TransformerException
	 */
	public String getSingleString(String xpath) throws TransformerException {
		return getSingleString(getDocument(), xpath);
		// return XPathAPI.eval(getDocument(), xpath, namespaceElement).str();
		// String str = null;
		// Node node = XPathAPI.selectSingleNode(getDocument(), xpath,
		// namespaceElement);
		// if (node != null) {
		// XObject xObject = XPathAPI.eval(node, "string()");
		// str = xObject.str();
		// }
		// return str;
	}

	public String getSingleString(Node node, String xpath)
			throws TransformerException {
		return XPathAPI.eval(node, xpath, namespaceElement).str();
	}

	/**
	 * Get a NodeList containing the nodes in the response DOM for the specified
	 * xpath
	 * 
	 * @param xpath
	 * @return the NodeList for the xpath into the response DOM
	 * @throws TransformerException
	 */
	public NodeList getNodeList(String xpath) throws TransformerException {
		return XPathAPI.selectNodeList(getDocument(), xpath, namespaceElement);
	}

	public void setAuthorizationString(String authorizationString) {
		this.authorizationString = authorizationString;
	}

	public String toString() {
		// Element docEl = getDocument().getDocumentElement();
		// return docEl.toString();
		Source input = new DOMSource(getDocument());
		StringWriter sw = new StringWriter();
		Result output = new StreamResult(sw);
		try {
			idTransformer.transform(input, output);
			return sw.toString();
		} catch (TransformerException e) {
			return e.getMessage();
		}
	}
}
