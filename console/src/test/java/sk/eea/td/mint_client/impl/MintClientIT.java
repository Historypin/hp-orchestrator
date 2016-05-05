package sk.eea.td.mint_client.impl;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.eea.td.IntegrationTest;
import sk.eea.td.mint_client.api.MintServiceException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(IntegrationTest.class)
public class MintClientIT {

	public static Logger LOG = LoggerFactory.getLogger(MintClientIT.class);
	private MintClientImpl client;
	private Integer datasetId = Integer.valueOf(0);

	@Before
	public void setUp() throws Exception {
		client = MintClientImpl.getNewClient("http://mint-projects.image.ntua.gr/foodanddrink");
	}

//	@Test
	public void test01_Login() {
		try {
			Boolean isLogin = client.login("histpin", "h1stp1n");
			assertTrue(isLogin);
		} catch (Exception e) {
			fail(e.toString());
		}
	}
	
//	@Test
	public void test02_Upload() {
		File f;
		test01_Login();
		try {
			f = new File(ClassLoader.getSystemResource("data.zip").toURI());
			datasetId = client.uploadJson(f);
			LOG.info("Dataset ID:" + datasetId);
			if(datasetId == null) fail();
			assertNotEquals(Integer.valueOf(0), datasetId);
		} catch (MintServiceException|URISyntaxException e) {
			fail(e.toString());
		}
	}
	
//	@Test
	public void test03_DefineItems(){
		test02_Upload();
		try {
			Boolean result = false;
			if(!Integer.valueOf(0).equals(datasetId)){
				result = client.defineItems(datasetId);
			}else{
				fail("Upload wasn't successful");
			}
			assertTrue(result);
			} catch (MintServiceException e) {
				fail(e.toString());
			}
	}
	
//	@Test
	public void test04_Transform(){
		test03_DefineItems();
		try {
			if(!Integer.valueOf(0).equals(datasetId)){
				Boolean result = client.transform(datasetId);
				assertTrue(result);
			}else{
				fail("Upload wasn't successful");
			}
		} catch (MintServiceException e) {
			fail(e.toString());
		}
	}
	
	@Test
	public void test05_Publish(){
		test04_Transform();
		try{
			if(!Integer.valueOf(0).equals(datasetId)){
				Boolean result = client.publish(datasetId);
				assertTrue(result);
			}else{
				fail("Upload wasn't successful");
			}
		} catch (MintServiceException e) {
			fail(e.toString());
		}
	}
	
	@After
	public void tearDown(){
		try {
			client.close();
		} catch (IOException e) {
			fail(e.toString());
		}
	}
}

