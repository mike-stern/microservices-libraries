package gov.usgs.cida.microservices.catalog;

import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import gov.usgs.cida.microservices.api.registration.RegistrationClient;
import gov.usgs.cida.microservices.config.ServiceConfig;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsulRegistrationClient implements RegistrationClient{
    private static final Logger logger = LoggerFactory.getLogger(ConsulRegistrationClient.class);
    @Override
    public void registerService(ServiceConfig configuration) {
	if (configuration == null) {
		throw new NullPointerException("Configuration may not be null");
	}

	String serviceName = configuration.getName();
	String serviceId = configuration.getId();
	Integer port = configuration.getPort();
	Long ttl = configuration.getTtl();
	String[] tags = configuration.getTags();


	if (StringUtils.isBlank(serviceName)) {
	    throw new IllegalStateException("Service name is required");
	}

	if (StringUtils.isBlank(serviceId)) {
	    serviceId = serviceName + UUID.randomUUID().toString();
	}

	if (port < 0) {
		throw new IllegalStateException("Service port is required to be a positive integer");
	}

	if (ttl <= 0) {
		throw new IllegalStateException("TTL needs to a positive value");
	}
	if (tags == null) {
		tags = new String[0];
	}

	AgentClient client = Consul.newClient(configuration.getAddress(), configuration.getPort()).agentClient();
		client.register(port, ttl, serviceName, serviceId, tags);
	logger.info("Registered new service: {}", serviceName);
    }

    @Override
    public void deregisterService(ServiceConfig configuration) {
	Consul client = Consul.newClient(configuration.getAddress(), configuration.getPort());
	AgentClient aClient = client.agentClient();
	//need to modify upstream api to permit deregistration given a known id
	aClient.deregister();
    }
    
}
