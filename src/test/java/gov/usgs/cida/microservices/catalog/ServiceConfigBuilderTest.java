package gov.usgs.cida.microservices.catalog;

import gov.usgs.cida.microservices.config.ServiceConfigBuilder;
import gov.usgs.cida.microservices.config.ServiceConfig;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author isuftin
 */
public class ServiceConfigBuilderTest {

	private static final Logger logger = LoggerFactory.getLogger(ServiceConfigBuilderTest.class);
	String name = "Test Instance";
	String id = "Test ID";
	String address = "127.0.0.1";
	int port = 8080;
	String[] tags = new String[]{"test-tag-1", "test-tag-2"};

	@Test
	public void testBuildWithAllParams() {
		logger.info("testBuildWithAllParams");
		ServiceConfigBuilder instance = new ServiceConfigBuilder();

		instance.setName(name)
		.setId(id)
		.setPort(port)
		.setAddress(address)
		.setTags(tags);
		ServiceConfig result = instance.build();
		assertNotNull(result);
		Assert.assertArrayEquals(result.getTags(), tags);
		assertEquals(result.getName(), name);
		assertEquals(result.getId(), id);
		assertEquals(result.getPort(), port);
	}
	@Test(expected = IllegalArgumentException.class)
	public void testBuildWithoutAddress() {
		logger.info("testBuildWithoutAddress");
		ServiceConfigBuilder instance = new ServiceConfigBuilder();

		instance.setName(name)
		.setId(id)
		.setPort(port)
		.setTags(tags);
		instance.build();
	}
	
	@Test(expected = IllegalStateException.class)
	public void testBuildWithoutName() {
		logger.info("testBuildWithoutName");
		ServiceConfigBuilder instance = new ServiceConfigBuilder();

		instance.setId(id)
		.setAddress(address)
		.setPort(port)
		.setTags(tags);
		instance.build(); // Should hit error here
	}
	
	@Test(expected = IllegalStateException.class)
	public void testBuildWithPortAtZero() {
		logger.info("testBuildWithPortAtZero");
		ServiceConfigBuilder instance = new ServiceConfigBuilder();

		instance.setName(name)
		.setAddress(address)
		.setId(id)
		.setPort(0)
		.setTags(tags);
		instance.build(); // Should hit error here
	}

	@Test
	public void testBuildWithOutId() {
		logger.info("testBuildWithOutId");
		ServiceConfigBuilder instance = new ServiceConfigBuilder();

		instance.setName(name)
		.setAddress(address)
		.setPort(port)
		.setTags(tags);
		ServiceConfig result = instance.build();
		assertNotNull(result);
		Assert.assertArrayEquals(result.getTags(), tags);
		assertEquals(result.getName(), name);
		assertNotEquals(result.getId(), id);
		assertEquals(result.getPort(), port);
	}

	@Test
	public void testBuildWithOutTtl() {
		logger.info("testBuildWithOutTtl");
		ServiceConfigBuilder instance = new ServiceConfigBuilder();

		instance.setName(name)
		.setAddress(address)
		.setId(id)
		.setPort(port)
		.setTags(tags);
		ServiceConfig result = instance.build();
		assertNotNull(result);
		Assert.assertArrayEquals(result.getTags(), tags);
		assertEquals(result.getName(), name);
		assertEquals(result.getId(), id);
		assertEquals(result.getPort(), port);
	}

}
