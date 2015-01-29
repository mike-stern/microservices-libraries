package gov.usgs.cida.microservices.catalog;

import gov.usgs.cida.microservices.registration.ConsulRegistrationClient;
import gov.usgs.cida.microservices.discovery.ConsulDiscoveryClient;
import gov.usgs.cida.microservices.api.discovery.DiscoveryClient;
import gov.usgs.cida.microservices.api.registration.RegistrationClient;
import gov.usgs.cida.microservices.config.ServiceConfig;
import gov.usgs.cida.microservices.config.ServiceConfigBuilder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.helpers.FileUtils;
import org.apache.http.client.utils.URIBuilder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
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
	static final String name = "cida-is-awesome-super-watermelon-test-name";
	static final String id = "cida-is-awesome-super-watermelon-test-id";
	static String address;
	static int port;
	static final String[] tags = new String[]{"cida-is-awesome-super-watermelon-test-tag-1", "cida-is-awesome-super-watermelon-test-tag-2"};
	static File tmpDir;
	static String node;
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
		String addressProperty = System.getProperty("address.consul");
		String portProperty = System.getProperty("port.consul");
		String nodeProperty = System.getProperty("node.consul");
		
		address = StringUtils.isNotBlank(addressProperty) ? addressProperty : "127.0.0.1";
		port = Integer.parseInt(StringUtils.isNotBlank(portProperty) ? portProperty : "8085");
		node = StringUtils.isNotBlank(nodeProperty) ? nodeProperty : "development-server.usgs.gov";
		
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
		p.destroyForcibly();
	}

	@Before
	public void setUp() throws IOException, InterruptedException {
		tmpDir = FileUtils.createTmpDir();
		String consulPath = System.getProperty("path.consul", "consul");
		String cmd = consulPath + " agent -server -bootstrap-expect=1 -data-dir=" + tmpDir.getCanonicalPath() + " -node " + node + " -bind=" + address + " -client=" + address;
		logger.info("running the following command in bash:" + cmd);
		p = Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", cmd});
		consulInputStream = p.getInputStream();
		Thread.sleep(3000);
		rClient = new ConsulRegistrationClient();
		rClient.registerService(config);
		dClient = new ConsulDiscoveryClient(address, 8500);
		Thread.sleep(1000);
	}

	@After
	public void tearDown() throws InterruptedException, IOException {
		p.destroy();
	}

	@Test
	public void testDiscoverServiceConfigs() throws InterruptedException, URISyntaxException {

		Map<String, Map<String, Set<ServiceConfig>>> services = dClient.getServiceConfigsForAllServices();
		Assert.assertFalse(services.isEmpty());
		assertTrue(services.containsKey(config.getName()));
		Map<String, Set<ServiceConfig>> serviceEntry = services.get(config.getName());
		assertEquals(serviceEntry.keySet().size(), config.getTags().length);
		for (String tag : config.getTags()) {
			assertTrue(serviceEntry.containsKey(tag));
			Set<ServiceConfig> versionConfigs = serviceEntry.get(tag);
			assertEquals(1, versionConfigs.size());
			ServiceConfig discoveredConfig = versionConfigs.iterator().next();
			assertTrue(discoveredConfig.equals(config));
		}
	}

	@Test
	public void testDiscoverServiceUris() throws URISyntaxException {
		URI expected = new URIBuilder().setHost(address).setPort(port).build();
		Map<String, Map<String, Set<URI>>> serviceUris = dClient.getUrisForAllServices();
		Assert.assertFalse(serviceUris.isEmpty());
		assertTrue(serviceUris.containsKey(config.getName()));
		Map<String, Set<URI>> serviceUriEntry = serviceUris.get(config.getName());
		assertEquals(serviceUriEntry.keySet().size(), config.getTags().length);
		for (String tag : config.getTags()) {
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
