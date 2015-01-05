package gov.usgs.cida.microservices.catalog;

import com.orbitz.consul.CatalogClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.catalog.CatalogService;
import gov.usgs.cida.microservices.api.discovery.Client;
import gov.usgs.cida.microservices.config.ServiceConfig;
import gov.usgs.cida.microservices.config.ServiceConfigBuilder;
import java.util.ArrayList;
import java.util.List;
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
	public Map<String, List<String>> getServices() {
		CatalogClient cClient = this.client.catalogClient();
		return cClient.getServices().getResponse();
	}

	@Override
	public List<ServiceConfig> getService(String serviceName) {
		CatalogClient cClient = this.client.catalogClient();
		List<ServiceConfig> result = new ArrayList<>();
		List<CatalogService> serviceList = cClient.getService(serviceName).getResponse();
		for (CatalogService service : serviceList) {
			ServiceConfigBuilder builder = new ServiceConfigBuilder();
			
			builder.setAddress(service.getAddress());
			builder.setNode(service.getNode());
			builder.setId(service.getServiceId());
			builder.setName(service.getServiceName());
			builder.setPort(service.getServicePort());
			builder.setTags(service.getServiceTags().toArray(new String[0]));
			
			result.add(builder.build());
		}
		
		return result;
	}
	
	
}
