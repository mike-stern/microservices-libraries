/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.microservices.catalog;

import gov.usgs.cida.microservices.api.registration.RegistrationClient;
import gov.usgs.cida.microservices.config.ServiceConfig;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author cschroed
 */
public class ConsulRegistrationClient implements RegistrationClient{

    @Override
    public static void registerService(ServiceConfig configuration) {
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
		this.serviceId = serviceId;
	} else {
		this.serviceId = UUID.randomUUID().toString();
	}

	if (port > 0) {
		this.port = port;
	} else {
		throw new IllegalStateException("Service port is required to be a positive integer");
	}

	if (ttl > 0) {
		this.ttl = ttl;
	} else {
		throw new IllegalStateException("TTL needs to a positive value");
	}
	if (tags != null) {
		this.tags = tags;
	} else {
		this.tags = new String[0];
	}

	this.client.agentClient().register(port, ttl, serviceName, serviceId, tags);
	logger.info("Registered new service: {}", this.serviceName);
    }

    @Override
    public void deregisterService(ServiceConfig configuration) {
	
    }
    
}
