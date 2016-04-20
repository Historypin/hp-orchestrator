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

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
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
			assertNotEquals(Integer.valueOf(0), datasetId);
		} catch (URISyntaxException e) {
			fail(e.toString());
		}
	}
	
//	@Test
	public void test03_DefineItems(){
		test02_Upload();
		if(!Integer.valueOf(0).equals(datasetId)){
			Boolean result = client.defineItems(datasetId);
			assertTrue(result);
		}else{
			fail("Upload wasn't successful");
		}
	}
	
//	@Test
	public void test04_Transform(){
		test03_DefineItems();
		if(!Integer.valueOf(0).equals(datasetId)){
			Boolean result = client.transform(datasetId);
			assertTrue(result);
		}else{
			fail("Upload wasn't successful");
		}
	}
	
	@Test
	public void test05_Publish(){
		test04_Transform();
		if(!Integer.valueOf(0).equals(datasetId)){
			Boolean result = client.publish(datasetId);
			assertTrue(result);
		}else{
			fail("Upload wasn't successful");
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

