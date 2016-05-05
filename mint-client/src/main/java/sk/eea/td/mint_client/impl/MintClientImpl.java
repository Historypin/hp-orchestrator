package sk.eea.td.mint_client.impl;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.ProxySelector;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.eea.td.mint_client.api.MintClient;
import sk.eea.td.mint_client.api.MintServiceException;

public class MintClientImpl implements Closeable, MintClient{
	
	/**
	 * Timeout in seconds for request to process.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(MintClientImpl.class);
	private String baseUrl;
	private CloseableHttpClient httpClient;
	private int timeout = 1800;

	public static MintClientImpl getNewClient(String baseUrl){
		return new MintClientImpl(baseUrl);
	}
	
	private MintClientImpl(String baseUrl) {
		this.baseUrl = baseUrl;
		this.httpClient = HttpClientBuilder.create().setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault()))
				.setRetryHandler(new DefaultHttpRequestRetryHandler(5, true))
				.build(); 
	}

	/* (non-Javadoc)
	 * @see sk.eea.td.mint_client.impl.MintClient#login()
	 */
	@Override
	public boolean login(String username, String password) {
		HttpPost httpPost = new HttpPost(baseUrl+"/Login.action");
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
		nvps.add(new BasicNameValuePair("username", username));
		nvps.add(new BasicNameValuePair("password", password));
		
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			CloseableHttpResponse resp = httpClient.execute(httpPost);
			boolean result = resp.containsHeader("Location");
			HttpEntity e = resp.getEntity();
			if( e != null ) {
				EntityUtils.consumeQuietly(e);
			}
			LOG.debug("Logging to MINT: "+(result?"SUCCESSFUL":"UNSUCCESSFUL"));
			return result;
		} catch( Exception e ) {
			return false;
		}
	}
	
	// return the datasetId or null if there is none
	/* (non-Javadoc)
	 * @see sk.eea.td.mint_client.impl.MintClient#uploadJson(java.io.File)
	 */
	@Override
	public Integer uploadJson( File f ) throws MintServiceException {
		// send the file
		int datasetId = -1;
		try {
			HttpPost httpPost = new HttpPost(baseUrl+"/AjaxFileReader.action?qqfile=" + 
					URLEncoder.encode( f.getName(), "UTF-8"));
			Path p = f.toPath();
			byte[] data = Files.readAllBytes(p);
			String tmpFileName = "";
			httpPost.setEntity(new ByteArrayEntity(data, ContentType.APPLICATION_OCTET_STREAM));
			httpPost.expectContinue();
			CloseableHttpResponse resp = httpClient.execute( httpPost );
			if( resp.getStatusLine().getStatusCode() == 200 ) {
				String respText = EntityUtils.toString( resp.getEntity());
				Pattern pattern  = Pattern.compile( "fname:\\s*'([^']+)'" );
				Matcher m = pattern.matcher( respText );
				if( m.find()) {
					tmpFileName = m.group(1);
				}
				EntityUtils.consumeQuietly(resp.getEntity());
			} else {
				LOG.debug("Uploading file to MINT: FAILED");
				return null;
			}

			httpPost = new HttpPost(baseUrl+"/Import.action" );
			List <NameValuePair> nvps = new ArrayList <NameValuePair>();
			nvps.add(new BasicNameValuePair("mth", "httpupload"));
			nvps.add(new BasicNameValuePair("upfile", tmpFileName ));
			nvps.add(new BasicNameValuePair("httpup",  f.getName() ));
			nvps.add(new BasicNameValuePair("isJson", "true" ));

			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			resp = httpClient.execute(httpPost);

			// find '// datasetId = \d+'
			if( resp.getStatusLine().getStatusCode() == 200 ) {
				String respText = EntityUtils.toString( resp.getEntity());
				Pattern pattern  = Pattern.compile( "\\n// datasetId = (\\d+)", Pattern.DOTALL );
				Matcher m = pattern.matcher( respText );
				if( m.find() ) {
					datasetId = Integer.parseInt( m.group(1));
				}
				EntityUtils.consumeQuietly(resp.getEntity());
				resp.close();
				if(!waitForReady(datasetId, timeout)){
					LOG.debug("Uploading file to MINT: FAILED");
					return null;
				}else{
					LOG.debug("Uploading file to MINT: SUCCESSFUL");
					return datasetId;
				}
			} else {
				EntityUtils.consumeQuietly(resp.getEntity());
				LOG.debug(MessageFormat.format("Result code: {0} when uploading file to MINT",resp.getStatusLine().getStatusCode()));
			}
		} catch( Exception e ) {
			throw new MintServiceException(e);
		}
		LOG.debug("Uploading file to MINT: FAILED");		
		return null;
	}
	
	// Use this to get all the status of the dataset
	/* (non-Javadoc)
	 * @see sk.eea.td.mint_client.impl.MintClient#completeStatus(int)
	 */
	@Override
	public JSONObject completeStatus( int id ) throws MintServiceException {
		CloseableHttpResponse resp = null;
		try {
			HttpGet get = new HttpGet( baseUrl+"/ImportStatusJson?importId="+id);
			resp = httpClient.execute( get );
			if( resp.getStatusLine().getStatusCode() == 200 ) {
				String respText = EntityUtils.toString( resp.getEntity());
				JSONObject js = new JSONObject(respText); 
				return js;
			}else{
				EntityUtils.consumeQuietly(resp.getEntity());
				return null;
			}
		} catch( Exception e ) {
			EntityUtils.consumeQuietly(resp.getEntity());
			throw new MintServiceException(e);
		}
	}
	
	// return the current status of the set
	// running, ready, error, ? 
	// loading, loading_failed, 
	/* (non-Javadoc)
	 * @see sk.eea.td.mint_client.impl.MintClient#findStatus(int)
	 */
	@Override
	public String findStatus( int datasetId ) throws MintServiceException {
		JSONObject js = completeStatus( datasetId );
		if( js == null ) return "error";
		if( js.getBoolean("inProgress" )){
			return "running";
		} else {
			if(js.has("derived")){
				JSONObject derived = js.getJSONObject("derived");
				for(String name : JSONObject.getNames(derived)){
					JSONObject derivedObject = derived.getJSONObject(name);
					if(derivedObject.getBoolean("inProgress")) return "running"; 
				}				
			}
		}
		return "ready";
	}
	
	// transform according to histroypin mapping
	/* (non-Javadoc)
	 * @see sk.eea.td.mint_client.impl.MintClient#transform(int)
	 */
	@Override
	public boolean transform( int datasetId ) throws MintServiceException {		
		HttpPost httpPost;
		CloseableHttpResponse resp = null;
		
		try {
			httpPost = new HttpPost(baseUrl+"/Transform.action" );
			List <NameValuePair> nvps = new ArrayList <NameValuePair>();
			nvps.add(new BasicNameValuePair("selectedMapping", HP_MAPPING ));
			nvps.add(new BasicNameValuePair("uploadId", Integer.toString( datasetId )));

			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			resp = httpClient.execute(httpPost);
			if( resp.getStatusLine().getStatusCode() != 200 ) {
				// problem ?
				LOG.warn(MessageFormat.format("Transform problem of dataset ({0}))",datasetId));
				LOG.debug("Transform error response" +EntityUtils.toString( resp.getEntity()));
				EntityUtils.consumeQuietly(resp.getEntity());
				return false;
			}else{
				return waitForReady(datasetId, timeout);
			}
		} catch( Exception e ) {
			LOG.error(MessageFormat.format("Transformation of MINT dataset ({0}) failed.", datasetId),e);
			EntityUtils.consumeQuietly(resp.getEntity());
			throw new MintServiceException(e);
		}
	}
	
	// when set is imported, send the correct define items
	/* (non-Javadoc)
	 * @see sk.eea.td.mint_client.impl.MintClient#defineItems(int)
	 */
	@Override
	public boolean defineItems( int datasetId ) throws MintServiceException {
		CloseableHttpResponse resp = null;
		try {
			HttpGet get = new HttpGet( new URIBuilder(baseUrl+"/Itemize")
					.addParameter("itemLevel", "/json")
					.addParameter( "itemLabel", "/json/caption")
					.addParameter( "itemNativeId", "/json/id")
					.addParameter( "uploadId", Integer.toString( datasetId ))
					.build());
			resp = httpClient.execute(get);
			int status = resp.getStatusLine().getStatusCode();
			EntityUtils.consumeQuietly( resp.getEntity());
			if( status == 200 ) {
				LOG.debug(MessageFormat.format("Define items for dataset ({0}) SUCCES", datasetId));
				return waitForReady(datasetId, timeout);
			}
		} catch( Exception e ) {
			LOG.error(MessageFormat.format("Problem definning items for dataset ({0}) in MINT",datasetId),e);
			if( resp != null ) EntityUtils.consumeQuietly( resp.getEntity());
			throw new MintServiceException(e);
		}
		LOG.error(MessageFormat.format("Problem definning items for dataset ({0}) in MINT",datasetId));
		return false;
	}
	
	// try to publish the given id
	// has to be transformed status should reflect that
	// http://mint-projects.image.ntua.gr/foodanddrink/XSLselection?uploadId=1960&orgId=1032&userId=-1
	/* (non-Javadoc)
	 * @see sk.eea.td.mint_client.impl.MintClient#publish(int)
	 */
	@Override
	public boolean publish( int datasetId ) throws MintServiceException {
		CloseableHttpResponse resp = null;
		try {
			HttpGet get = new HttpGet( new URIBuilder(baseUrl+"/XSLselection")
				.addParameter( "uploadId", Integer.toString( datasetId ))
				.build());
			resp = httpClient.execute(get);
			int status = resp.getStatusLine().getStatusCode();
			EntityUtils.consumeQuietly( resp.getEntity());
			if( status == 200 ) {
				LOG.debug(MessageFormat.format("Publishing of dataset ({0}) SUCCESSFUL", datasetId));
				return waitForReady(datasetId, timeout);
			}	
		} catch( Exception e ) {
			LOG.error(MessageFormat.format("Problem publishing items of dataset ({0}).", datasetId), e);
			if( resp != null ) EntityUtils.consumeQuietly( resp.getEntity());
			throw new MintServiceException(e);
		}
		LOG.error(MessageFormat.format("Problem publishing items of dataset ({0}).", datasetId));
		return false;
	}
	
	
	//Wait for id dataset to become ready (not running) but only timeOutSeconds
	// return true on ready, false on timeout or error
	/* (non-Javadoc)
	 * @see sk.eea.td.mint_client.impl.MintClient#waitForReady(int, int)
	 */
	@Override
	public boolean waitForReady( int id, int timeOutSeconds ) {
		long start = System.currentTimeMillis();
		while( true ) {
			try {
				Thread.sleep(2000);
				String status = findStatus(id);
				if( "ready".equals( status )) return true;
				if(	"error".equals( status )) return false;
				if( System.currentTimeMillis() - start - 1000*timeOutSeconds > 0 ) return false;
			} catch( Exception e ) {
				LOG.debug("Could not get dataset state.", e);
				return false;
			}
		}
	}


	@Override
	public void close() throws IOException {
		httpClient.close();
	}
}
