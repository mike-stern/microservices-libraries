/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.microservices.catalog;

import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author isuftin
 */
public class ConsulCatalogClientTest {
	
	public ConsulCatalogClientTest() {
	}
	
	@BeforeClass
	public static void setUpClass() {
	}
	
	@AfterClass
	public static void tearDownClass() {
	}
	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
	}

	@Test
	@Ignore
	public void testRegister() {
		System.out.println("register");
		String serviceId = "testId";
		String serviceName = "testName";
		ConsulCatalogClient instance = new ConsulCatalogClient("127.0.0.1", 8500);
		instance.registerService(serviceId, serviceName, 8080);
		assertTrue("Passed", true);
	}
	
	@Test
	@Ignore
	public void testGetCatalogService() {
		ConsulCatalogClient instance = new ConsulCatalogClient("127.0.0.1", 8500);
		Map<String, String> service = instance.getServiceByName("testId");
		assertTrue(service.size() == 4);
	}
	
}
