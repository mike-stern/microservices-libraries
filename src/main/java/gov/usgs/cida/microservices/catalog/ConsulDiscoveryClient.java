package gov.usgs.cida.microservices.catalog;

import com.orbitz.consul.CatalogClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.catalog.CatalogService;
import gov.usgs.cida.microservices.api.discovery.Client;
import gov.usgs.cida.microservices.api.discovery.DiscoveryClient;
import gov.usgs.cida.microservices.config.ServiceConfig;
import gov.usgs.cida.microservices.config.ServiceConfigBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.security.x509.IPAddressName;

public class ConsulClient implements DiscoveryClient {

	private static final Logger logger = LoggerFactory.getLogger(ConsulClient.class);
	private ArrayList<Consul> clients = new ArrayList<>();
	
	/**
	 * Creates a client that communicates with only one consul node
	 *
	 * @param ipAddress
	 */
	public ConsulClient(String ipAddress, int port) {
	    addClient(ipAddress, port);
	}
	/**
	 * Creates a client that communicates with many consul nodes on different
	 * IP addresses, but identical ports
	 * 
	 * @param ipAddress
	 * @param port 
	 */
	public ConsulClient(Collection<String> ipAddresses, int port){
	    for(String ipAddress : ipAddresses){
		addClient(ipAddress, port);
	    }
	}
	private void addClient(String ipAddress, int port){
	    this.clients.add(Consul.newClient(ipAddress, port));
	}
	/**
	 * Creates a client that communicates with many consul nodes on different
	 * IP addresses and different ports
	 * 
	 * @param ipAddressToPort 
	 */
	public ConsulClient(Map<String, Integer> ipAddressToPort){
	    for(Map.Entry<String, Integer> entry : ipAddressToPort.entrySet()){
		this.clients.add(Consul.newClient(entry.getKey(), entry.getValue()));
	    }
	}

	public List<ServiceConfig> getService(String serviceName) {
		CatalogClient cClient = this.client.catalogClient();
		List<ServiceConfig> result = new ArrayList<>();
		List<CatalogService> serviceList = cClient.getService(serviceName).getResponse();
		for (CatalogService service : serviceList) {
			ServiceConfigBuilder builder = new ServiceConfigBuilder();
			
			builder.setAddress(service.getAddress())
			.setNode(service.getNode())
			.setId(service.getServiceId())
			.setName(service.getServiceName())
			.setPort(service.getServicePort())
			.setTags(service.getServiceTags().toArray(new String[0]));
			
			result.add(builder.build());
		}
		
		return result;
	}

    @Override
    public Map<String, Map<String, Collection<URI>>> getServices() {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<URI> getUrisFor(String serviceName, String version) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public URI getUriFor(String serviceName, String version) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
	
	
}
