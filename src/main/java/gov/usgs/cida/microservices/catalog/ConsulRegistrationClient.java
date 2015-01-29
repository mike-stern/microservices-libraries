package gov.usgs.cida.microservices.catalog;

import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.agent.Registration;
import gov.usgs.cida.microservices.api.registration.RegistrationClient;
import gov.usgs.cida.microservices.config.ServiceConfig;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsulRegistrationClient implements RegistrationClient {

	private static final Logger logger = LoggerFactory.getLogger(ConsulRegistrationClient.class);
	public static final int DEFAULT_CONSUL_PORT = 8500;
	public static final String DEFAULT_CONSUL_ADDRESS = "localhost";
	public static final long DEFAULT_CONSUL_TTL = 10L;
	private final AgentClient agentClient;

	public ConsulRegistrationClient() {
		this(DEFAULT_CONSUL_ADDRESS, DEFAULT_CONSUL_PORT);
	}

	public ConsulRegistrationClient(int consulPort) {
		this(DEFAULT_CONSUL_ADDRESS, consulPort);
	}

	public ConsulRegistrationClient(String consulAddress) {
		this(consulAddress, DEFAULT_CONSUL_PORT);
	}

	public ConsulRegistrationClient(String consulAddress, int consulPort) {
		agentClient = Consul.newClient(consulAddress, consulPort).agentClient();
	}

	@Override
	public void registerService(ServiceConfig config) {
		registerService(config, DEFAULT_CONSUL_PORT);
	}

	/**
	 *
	 * @param config
	 * @param consulPort the port on which the consul http api listens --
	 * distinct from the service's port
	 */
	public void registerService(ServiceConfig config, int consulPort) {
		if (config == null) {
			throw new NullPointerException("Configuration may not be null");
		}

		Registration reg = new Registration();
		reg.setName(config.getName());
		reg.setPort(config.getPort());
		reg.setId(config.getId());
		reg.setTags(config.getTags());

		//todo: make Check configurable
		Registration.Check check = new Registration.Check();
		check.setTtl(DEFAULT_CONSUL_TTL + "s");
		reg.setCheck(check);

		agentClient.register(reg);
		logger.info("Registered new service: {}({})@{}:{} with id: {}", config.getName(), config.getTags(), config.getAddress(), config.getPort(), config.getId());
	}

	@Override
	public void deregisterService(ServiceConfig configuration) {
		String id = configuration.getId();
		if (StringUtils.isBlank(id)) {
			throw new IllegalArgumentException("the service must have an id");
		} else {
			deregisterService(configuration.getId());
		}

	}

	public void deregisterService(String id) {
		agentClient.deregister(id);
		logger.info("Deregistered service: {}", id);
	}

}
