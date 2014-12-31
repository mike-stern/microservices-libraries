package gov.usgs.cida.microservices.catalog;

import gov.usgs.cida.microservices.config.ServiceConfigBuilder;
import gov.usgs.cida.microservices.config.ServiceConfig;
import gov.usgs.cida.microservices.api.discovery.Client;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import org.apache.cxf.helpers.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
	String name = "Test Instance";
	String id = "Test ID";
	int port = 8080;
	long ttl = 5l;
	String[] tags = new String[]{"test-tag-1", "test-tag-2"};

	public ConsulClientTestIT() {
	}

	@BeforeClass
	public static void setUpClass() throws IOException {
		p = Runtime.getRuntime().exec(new String[]{"bash", "-c", consulLocation + " agent -server -bootstrap-expect 1 -data-dir /tmp/consul -node unitTest"});
		consulInputStream = p.getInputStream();
	}

	@AfterClass
	public static void tearDownClass() {
		try {
			consulInputStream.close();
		} catch (IOException ex) {
			// Meh
		}
		p.destroy();
	}

	@Before
	public void setUp() throws IOException {

	}

	@After
	public void tearDown() {

	}

	@Test
	public void testRegisterService() {
		logger.info("testRegisterService");
		ServiceConfigBuilder builder = new ServiceConfigBuilder();

		builder.setName(name);
		builder.setId(id);
		builder.setPort(port);
		builder.setTtl(ttl);
		builder.setTags(tags);
		ServiceConfig config = builder.build();

		Client client = new ConsulClient(config);
		client.registerService();
		Map<String, ServiceConfig> services = client.getServices();
		Assert.assertFalse(services.isEmpty());
		assertEquals(services.keySet().size(), 2);
	}

	@Test
	public void testDeRegisterService() {
		logger.info("testDeRegisterService");
		ServiceConfigBuilder builder = new ServiceConfigBuilder();

		builder.setName(name);
		builder.setId(id);
		builder.setPort(port);
		builder.setTtl(ttl);
		builder.setTags(tags);
		ServiceConfig config = builder.build();

		Client client = new ConsulClient(config);
		client.registerService();
		Map<String, ServiceConfig> services = client.getServices();
		assertFalse(services.isEmpty());
		assertEquals(services.keySet().size(), 2);

		client.deregisterService();
		services = client.getServices();
		assertEquals(services.keySet().size(), 1);
	}

}
