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
    public static final int DEFAULT_CONSUL_PORT = 8500;
    public static final String DEFAULT_CONSUL_ADDRESS = "localhost";
    private final AgentClient agentClient;
    
    public ConsulRegistrationClient(){
	this(DEFAULT_CONSUL_ADDRESS, DEFAULT_CONSUL_PORT);
    }
    public ConsulRegistrationClient(int consulPort){
	this(DEFAULT_CONSUL_ADDRESS, consulPort);
    }
    public ConsulRegistrationClient(String consulAddress){
	this(consulAddress, DEFAULT_CONSUL_PORT);
    }
    public ConsulRegistrationClient(String consulAddress, int consulPort){
	agentClient = Consul.newClient(consulAddress, consulPort).agentClient();
    }
    @Override
    public void registerService(ServiceConfig config) {
	registerService(config, DEFAULT_CONSUL_PORT);
    }
    /**
     * 
     * @param config
     * @param consulPort the port on which the consul http api listens -- distinct from the service's port
     */
    public void registerService(ServiceConfig config, int consulPort) {
	if (config == null) {
		throw new NullPointerException("Configuration may not be null");
	}
	agentClient.register(config.getPort(), config.getTtl(), config.getName(), config.getName(), config.getTags());
	logger.info("Registered new service: {}", config.getName());
    }

    @Override
    public void deregisterService(ServiceConfig configuration) {
	String id = configuration.getId();
	if(StringUtils.isBlank(id)){
	    throw new IllegalArgumentException("the service must have an id");
	}
	else{
	    deregisterService(configuration.getId());
	}

    }
    public void deregisterService(String id) {
	agentClient.deregister(id);
	logger.info("Deregistered service: {}", id);
    }
    
}
