package gov.usgs.cida.microservices.catalog;

import com.orbitz.consul.CatalogClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.catalog.CatalogService;
import com.orbitz.consul.option.CatalogOptions;
import com.orbitz.consul.option.CatalogOptionsBuilder;
import gov.usgs.cida.microservices.api.discovery.DiscoveryClient;
import gov.usgs.cida.microservices.config.ServiceConfig;
import gov.usgs.cida.microservices.config.ServiceConfigBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsulDiscoveryClient implements DiscoveryClient {
	
	private static final Logger logger = LoggerFactory.getLogger(ConsulDiscoveryClient.class);
	private List<CatalogClient> clients = new ArrayList<>();
	private static final Random random = new Random();
	
	
	private void addClient(String ipAddress, int port){
	    this.clients.add(Consul.newClient(ipAddress, port).catalogClient());
	}
	
	private CatalogClient getClient(){
	    return getRandomElement(clients);
	}
	
	private static <T> T getRandomElement(List<T> list){
	    int randomIndex = random.nextInt(list.size());
	    return list.get(randomIndex);
	} 
	
	/**
	 * Creates a client that communicates with only one consul node
	 *
	 * @param ipAddress
	 */
	public ConsulDiscoveryClient(String ipAddress, int port) {
	    addClient(ipAddress, port);
	}
	
	/**
	 * Creates a client that communicates with many consul nodes on different
	 * IP addresses, but identical ports. Traffic is distributed to the nodes
	 * randomly.
	 * 
	 * @param ipAddress
	 * @param port 
	 */
	public ConsulDiscoveryClient(Collection<String> ipAddresses, int port){
	    for(String ipAddress : ipAddresses){
		addClient(ipAddress, port);
	    }
	}
	
	/**
	 * Creates a client that communicates with many consul nodes on different
	 * IP addresses and different ports. Traffic is distributed to the nodes
	 * randomly.
	 * 
	 * @param ipAddressToPort 
	 */
	public ConsulDiscoveryClient(Map<String, Integer> ipAddressToPort){
	    for(Map.Entry<String, Integer> entry : ipAddressToPort.entrySet()){
		addClient(entry.getKey(), entry.getValue());
	    }
	}

	public List<ServiceConfig> getService(String serviceName) {
		CatalogClient cClient = getClient();
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
    public Map<String, Map<String, Collection<URI>>> getAllServices() {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<URI> getUrisFor(String serviceName, String version) {
	List<CatalogService> services = getServices(serviceName, version);
	List<URI> uris = new ArrayList(services.size());
	for(CatalogService service : services){
	    uris.add(buildServiceUri(service));
	}
	return uris;
    }
    private List<CatalogService> getServices(String serviceName, String version){
	CatalogClient catClient = getClient();
	CatalogOptions catOpts;
	catOpts = CatalogOptionsBuilder.builder().tag(version).build();
	List<CatalogService> services = catClient.getService(serviceName, catOpts).getResponse();
	return services;
    }
    private URI buildServiceUri(CatalogService catService){
	String address = catService.getAddress();
	int port = catService.getServicePort();
	URIBuilder uriBuilder = new URIBuilder();
	uriBuilder.setHost(address);
	uriBuilder.setPort(port);
	URI uri;
	try {
	    uri = uriBuilder.build();
	} catch (URISyntaxException ex) {
	    throw new RuntimeException("Error Building URI from information provided by consul", ex);
	}
	return uri;
    }
    
    @Override
    public URI getUriFor(String serviceName, String version) {
	List<CatalogService> services = getServices(serviceName, version);
	CatalogService catService = getRandomElement(services);
	URI uri = buildServiceUri(catService);
	return uri;
    }
	
}
