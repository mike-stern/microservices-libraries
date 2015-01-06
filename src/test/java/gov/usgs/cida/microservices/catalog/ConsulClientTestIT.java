package gov.usgs.cida.microservices.catalog;

import gov.usgs.cida.microservices.api.discovery.Client;
import gov.usgs.cida.microservices.config.ServiceConfig;
import gov.usgs.cida.microservices.config.ServiceConfigBuilder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.cxf.helpers.FileUtils;
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

/**
 *
 * @author isuftin
 */
public class ConsulClientTestIT {

	private static final Logger logger = LoggerFactory.getLogger(ConsulClientTestIT.class);
	static Process p;
	static InputStream consulInputStream;
	static String consulLocation = "/usr/local/bin/consul";
	String name = "test_instance";
	String id = "test_id";
	int port = 8080;
	long ttl = 5l;
	String[] tags = new String[]{"test-tag-1", "test-tag-2"};
	static File tmpDir;
	private static final String IPADDRESS_PATTERN
			= "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

	public ConsulClientTestIT() {
	}

	@BeforeClass
	public static void setUpClass() throws IOException {

	}

	@AfterClass
	public static void tearDownClass() {

	}

	@Before
	public void setUp() throws IOException, InterruptedException {
		tmpDir = FileUtils.createTmpDir();
		p = Runtime.getRuntime().exec(new String[]{"bash", "-c", consulLocation + " agent -server -bootstrap-expect 1 -data-dir " + tmpDir.getCanonicalPath() + " -node unitTest"});
		consulInputStream = p.getInputStream();
		Thread.sleep(3000);
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
	}

	@Test
	public void testRegisterService() throws InterruptedException {
		logger.info("testRegisterService");
		ServiceConfigBuilder builder = new ServiceConfigBuilder();

		builder.setName(name)
		.setId(id)
		.setPort(port)
		.setTtl(ttl)
		.setTags(tags);
		ServiceConfig config = builder.build();

		Client client = new ConsulClient(config);
		client.registerService();
		Thread.sleep(1000);

		Map<String, List<String>> services = client.getServices();
		Assert.assertFalse(services.isEmpty());
		assertEquals(2, services.keySet().size());
	}

	@Test
	public void testDeRegisterService() throws InterruptedException {
		logger.info("testDeRegisterService");
		ServiceConfigBuilder builder = new ServiceConfigBuilder();

		builder.setName(name)
		.setId(id)
		.setPort(port)
		.setTtl(ttl)
		.setTags(tags);
		ServiceConfig config = builder.build();

		Client client = new ConsulClient(config);
		client.registerService();
		Thread.sleep(1000);

		Map<String, List<String>> services = client.getServices();
		assertFalse(services.isEmpty());
		assertEquals(2, services.keySet().size());

		client.deregisterService();
		Thread.sleep(1000);

		services = client.getServices();
		assertEquals(1, services.keySet().size());
	}

	@Test
	public void testGetServiceByName() throws InterruptedException {
		logger.info("testGetServiceByName");
		ServiceConfigBuilder builder = new ServiceConfigBuilder();
		Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);

		builder.setName(name)
		.setId(id)
		.setPort(port)
		.setTtl(ttl)
		.setTags(tags);
		ServiceConfig config = builder.build();

		Client client = new ConsulClient(config);
		client.registerService();
		Thread.sleep(1000);

		List<ServiceConfig> service = client.getService(name);
		assertEquals(1, service.size());
		assertTrue(pattern.matcher(service.get(0).getAddress()).matches());
		assertEquals(8080, service.get(0).getPort());
	}

}
