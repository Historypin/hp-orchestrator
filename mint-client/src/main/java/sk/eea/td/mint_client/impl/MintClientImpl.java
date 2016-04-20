package sk.eea.td.mint_client.impl;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
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
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.eea.td.mint_client.api.MintClient;

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
		this.httpClient = HttpClientBuilder.create().build(); 
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
	public Integer uploadJson( File f ) {
		// send the file
		try {
			HttpPost httpPost = new HttpPost(baseUrl+"/AjaxFileReader.action?qqfile=" + 
					URLEncoder.encode( f.getName(), "UTF-8"));
			Path p = f.toPath();
			byte[] data = Files.readAllBytes(p);
			String tmpFileName = "";
			int datasetId = -1;
			httpPost.setEntity(new ByteArrayEntity(data, ContentType.APPLICATION_OCTET_STREAM));
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
				waitForReady(datasetId, timeout);
				return datasetId;
			} else {
				EntityUtils.consumeQuietly(resp.getEntity());
				return null;
			}
		} catch( Exception e ) {
			e.printStackTrace();
			return null;
		}
	}
	
	// Use this to get all the status of the dataset
	/* (non-Javadoc)
	 * @see sk.eea.td.mint_client.impl.MintClient#completeStatus(int)
	 */
	@Override
	public JSONObject completeStatus( int id ) {
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
			e.printStackTrace();
			EntityUtils.consumeQuietly(resp.getEntity());			
		}
		return null;
	}
	
	// return the current status of the set
	// running, ready, error, ? 
	// loading, loading_failed, 
	/* (non-Javadoc)
	 * @see sk.eea.td.mint_client.impl.MintClient#findStatus(int)
	 */
	@Override
	public String findStatus( int datasetId ) {
		JSONObject js = completeStatus( datasetId );
		if( js == null ) return "error";
		if( js.getBoolean("inProgress" )) return "running"; 
		else return "ready";
	}
	
	// transform according to histroypin mapping
	/* (non-Javadoc)
	 * @see sk.eea.td.mint_client.impl.MintClient#transform(int)
	 */
	@Override
	public boolean transform( int datasetId ) {		
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
				LOG.warn("Transform problem: \n"+ EntityUtils.toString( resp.getEntity()));
				EntityUtils.consumeQuietly(resp.getEntity());						
				return false;
			}else{
				return waitForReady(datasetId, timeout);
			}
		} catch( Exception e ) {
			e.printStackTrace();
			EntityUtils.consumeQuietly(resp.getEntity());						
		}

		return false;
	}
	
	// when set is imported, send the correct define items
	/* (non-Javadoc)
	 * @see sk.eea.td.mint_client.impl.MintClient#defineItems(int)
	 */
	@Override
	public boolean defineItems( int datasetId ) {
		CloseableHttpResponse resp = null;
		try {
			HttpGet get = new HttpGet( new URIBuilder(baseUrl+"/Itemize")
					.addParameter("itemLevel", "/json/results")
					.addParameter( "itemLabel", "/json/results/caption")
					.addParameter( "itemNativeId", "/json/results/id")
					.addParameter( "uploadId", Integer.toString( datasetId ))
					.build());
			resp = httpClient.execute(get);
			int status = resp.getStatusLine().getStatusCode();
			EntityUtils.consumeQuietly( resp.getEntity());
			if( status == 200 ) {
				return waitForReady(datasetId, timeout);
			}
		} catch( Exception e ) {
			e.printStackTrace();
			if( resp != null ) EntityUtils.consumeQuietly( resp.getEntity());
			return false;
		}
		return false;
	}
	
	// try to publish the given id
	// has to be transformed status should reflect that
	// http://mint-projects.image.ntua.gr/foodanddrink/XSLselection?uploadId=1960&orgId=1032&userId=-1
	/* (non-Javadoc)
	 * @see sk.eea.td.mint_client.impl.MintClient#publish(int)
	 */
	@Override
	public boolean publish( int datasetId ) {
		CloseableHttpResponse resp = null;
		try {
			HttpGet get = new HttpGet( new URIBuilder(baseUrl+"/XSLselection")
				.addParameter( "uploadId", Integer.toString( datasetId ))
				.build());
			resp = httpClient.execute(get);
			int status = resp.getStatusLine().getStatusCode();
			EntityUtils.consumeQuietly( resp.getEntity());
			if( status == 200 ) {
				return waitForReady(datasetId, timeout);
			}	
		} catch( Exception e ) {
			e.printStackTrace();
			if( resp != null ) EntityUtils.consumeQuietly( resp.getEntity());
			return false;
		}
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
				e.printStackTrace();
				return false;
			}
		}
	}


	@Override
	public void close() throws IOException {
		httpClient.close();
	}
}