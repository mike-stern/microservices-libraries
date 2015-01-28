package gov.usgs.cida.microservices.catalog;

import gov.usgs.cida.microservices.api.discovery.DiscoveryClient;
import gov.usgs.cida.microservices.api.registration.RegistrationClient;
import gov.usgs.cida.microservices.config.ServiceConfig;
import gov.usgs.cida.microservices.config.ServiceConfigBuilder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.cxf.helpers.FileUtils;
import org.apache.http.client.utils.URIBuilder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsulRegistrationAndDiscoveryClientsTestIT {

	private static final Logger logger = LoggerFactory.getLogger(ConsulRegistrationAndDiscoveryClientsTestIT.class);
	static Process p;
	static InputStream consulInputStream;
	static final String name = "test-name";
	static final String id = "test-id";
	static final String address = "127.0.0.1";
	static final int port = 8080;
	static final String[] tags = new String[]{"test-tag-1", "test-tag-2"};
	static File tmpDir;
	static final String node = "test-node";
	private static final String IPADDRESS_PATTERN
			= "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	public static ServiceConfig config;
	static DiscoveryClient dClient;
	static RegistrationClient rClient;
	
	public ConsulRegistrationAndDiscoveryClientsTestIT() {
	}

	@BeforeClass
	public static void setUpClass() throws IOException {
	    ServiceConfigBuilder builder = new ServiceConfigBuilder();
		builder.setName(name)
		.setNode(node)
		.setId(id)
		.setPort(port)
		.setAddress(address)
		.setTags(tags);
		config = builder.build();

	}

	@AfterClass
	public static void tearDownClass() {

	}

	@Before
	public void setUp() throws IOException, InterruptedException {
		tmpDir = FileUtils.createTmpDir();
		String cmd = "consul agent -server -bootstrap-expect 1 -data-dir " + tmpDir.getCanonicalPath() + " -node "+ node + " -bind=" + address +" -client=" + address;
		logger.info("running the following command in bash:" + cmd);
		p = Runtime.getRuntime().exec(new String[]{"bash", "-c", cmd});
		consulInputStream = p.getInputStream();
		Thread.sleep(3000);
		rClient = new ConsulRegistrationClient();
		rClient.registerService(config);
		Thread.sleep(1000);
		dClient = new ConsulDiscoveryClient(address, 8500);
	}

	@After
	public void tearDown() throws InterruptedException {
		try {
			consulInputStream.close();
		} catch (IOException ex) {
			// Meh
		}
		p.destroy();
		Thread.sleep(3000);

		FileUtils.delete(tmpDir);
		logger.debug("deleted temp dir");
	}
	
	@Test
	public void testDiscoverServiceConfigs() throws InterruptedException, URISyntaxException {
		
		Map<String, Map<String, Set<ServiceConfig>>> services = dClient.getServiceConfigsForAllServices();
		Assert.assertFalse(services.isEmpty());
		//'consul' service + number of services registered before each test method = 2
		assertEquals(2, services.keySet().size());
		assertTrue(services.containsKey(config.getName()));
		Map<String, Set<ServiceConfig>> serviceEntry = services.get(config.getName());
		assertEquals(serviceEntry.keySet().size(), config.getTags().length);
		for(String tag : config.getTags()){
		    assertTrue(serviceEntry.containsKey(tag));
		    Set<ServiceConfig> versionConfigs = serviceEntry.get(tag);
		    assertEquals(1, versionConfigs.size());
		    ServiceConfig discoveredConfig = versionConfigs.iterator().next();
		    assertTrue(discoveredConfig.equals(config));
		}
		
		
	}
	@Test
	public void testDiscoverServiceUris () throws URISyntaxException{
		URI expected = new URIBuilder().setHost(address).setPort(port).build();
		Map<String, Map<String, Set<URI>>> serviceUris = dClient.getUrisForAllServices();
		Assert.assertFalse(serviceUris.isEmpty());
		assertEquals(2, serviceUris.keySet().size());
		assertTrue(serviceUris.containsKey(config.getName()));
		Map<String, Set<URI>> serviceUriEntry = serviceUris.get(config.getName());
		assertEquals(serviceUriEntry.keySet().size(), config.getTags().length);
		for(String tag : config.getTags()){
		    assertTrue(serviceUriEntry.containsKey(tag));
		    Set<URI> versionUris = serviceUriEntry.get(tag);
		    assertEquals(1, versionUris.size());
		    URI discoveredUri = versionUris.iterator().next();
		    assertEquals(expected, discoveredUri);
		}
	}
	@Test
	public void testDeRegisterService() throws InterruptedException {
		
		rClient.deregisterService(config);
		Thread.sleep(1000);
		ServiceConfig svc = dClient.getServiceConfigFor(config.getName(), config.getTags()[0]);
		Assert.assertNull(svc);
		URI uri = dClient.getUriFor(config.getName(), config.getTags()[0]);
		Assert.assertNull(uri);
	}
}
