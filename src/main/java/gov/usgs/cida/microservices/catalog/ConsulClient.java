package gov.usgs.cida.microservices.catalog;

import com.orbitz.consul.Consul;
import com.orbitz.consul.model.health.Service;
import gov.usgs.cida.microservices.api.discovery.Client;
import gov.usgs.cida.microservices.config.ServiceConfig;
import gov.usgs.cida.microservices.config.ServiceConfigBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsulClient implements Client {

	private static final Logger logger = LoggerFactory.getLogger(ConsulClient.class);
	private static final String clientHost = "127.0.0.1"; // TODO - Should come from config
	private static final int clientPort = 8500; // TODO - Should come from config
	private String serviceName;
	private String serviceId;
	private Integer port;
	private Long ttl;
	private String[] tags;
	private Consul client;
	
	
	/**
	 * Creates a registration client using ServiceConfig
	 *
	 * @param configuration service configuration object
	 */
	public ConsulClient(ServiceConfig configuration) {
		if (configuration == null) {
			throw new NullPointerException("Configuration may not be null");
		}

		String _serviceName = configuration.getName();
		String _serviceId = configuration.getId();
		Integer _port = configuration.getPort();
		Long _ttl = configuration.getTtl();
		String[] _tags = configuration.getTags();

		if (StringUtils.isNotBlank(_serviceName)) {
			this.serviceName = _serviceName;
		} else {
			throw new IllegalStateException("Service name is required");
		}

		if (StringUtils.isNotBlank(_serviceId)) {
			this.serviceId = _serviceId;
		} else {
			this.serviceId = UUID.randomUUID().toString();
		}

		if (_port > 0) {
			this.port = _port;
		} else {
			throw new IllegalStateException("Service port is required to be a positive integer");
		}

		if (_ttl > 0) {
			this.ttl = _ttl;
		} else {
			throw new IllegalStateException("TTL needs to a positive value");
		}
		if (_tags != null) {
			this.tags = _tags;
		} else {
			this.tags = new String[0];
		}
		
		this.client = Consul.newClient(clientHost, clientPort);
	}

	@Override
	public void registerService() {
		this.client.agentClient().register(port, ttl, serviceName, serviceId, tags);
		logger.info("Registered new service: {}", this.serviceName);
	}

	@Override
	public void deregisterService() {
		this.client.agentClient().deregister();
	}

	@Override
	public Map<String, ServiceConfig> getServices() {
		Map<String, ServiceConfig> result = new HashMap<>();
		Map<String, Service> services = this.client.agentClient().getServices();
		for (String key : services.keySet()) {
			ServiceConfigBuilder confBuilder = new ServiceConfigBuilder();
			Service svc = services.get(key);
			
			confBuilder.setId(svc.getId());
			confBuilder.setName(svc.getService());
			confBuilder.setPort(svc.getPort());
			confBuilder.setTags(svc.getTags());
			
			result.put(key, confBuilder.build());
		}
		return result;
	}
}
