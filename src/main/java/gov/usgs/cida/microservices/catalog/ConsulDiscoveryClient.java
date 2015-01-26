package gov.usgs.cida.microservices.catalog;

import com.orbitz.consul.CatalogClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.catalog.CatalogService;
import com.orbitz.consul.option.CatalogOptions;
import com.orbitz.consul.option.CatalogOptionsBuilder;
import gov.usgs.cida.microservices.api.discovery.DiscoveryClient;
import gov.usgs.cida.microservices.config.ConsulCatalogServiceConfigBuilder;
import gov.usgs.cida.microservices.config.ServiceConfig;
import gov.usgs.cida.microservices.config.ServiceConfigBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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

    @Override
    public Map<String, Map<String, List<URI>>> getUrisForAllServices() {
	Map<String, Map<String, List<URI>>> returnMap = new HashMap<>();
	Map<String, Map<String, List<ServiceConfig>>> allServiceConfigs = getServiceConfigsForAllServices();
	for(Map.Entry<String, Map<String, List<ServiceConfig>>> serviceToTagMap : allServiceConfigs.entrySet()){
	    String serviceName = serviceToTagMap.getKey();
	    Map<String, List<URI>> tagAndUriMap = new HashMap<>();
	    Map<String, List<ServiceConfig>> tagToServiceConfig = serviceToTagMap.getValue();
	    for(Map.Entry<String, List<ServiceConfig>> innerEntry : tagToServiceConfig.entrySet()){
		String tag = innerEntry.getKey();
		List<URI> tagSpecificUris = new ArrayList<>();
		for(ServiceConfig serviceConfig : innerEntry.getValue()){
		    tagSpecificUris.add(buildServiceUri(serviceConfig));
		}
		tagAndUriMap.put(tag, tagSpecificUris);
	    }
	    returnMap.put(serviceName, tagAndUriMap);
	}
	return returnMap;
    }

    @Override
    public List<URI> getUrisFor(String serviceName, String version) {
	List<ServiceConfig> services = getServiceConfigsFor(serviceName, version);
	List<URI> uris = new ArrayList(services.size());
	for(ServiceConfig service : services){
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
    private URI buildServiceUri(ServiceConfig svcConfig){
	String address = svcConfig.getAddress();
	int port = svcConfig.getPort();
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
	ServiceConfig service = getServiceConfigFor(serviceName, version);
	URI uri = buildServiceUri(service);
	return uri;
    }

    @Override
    public Map<String, Map<String, List<ServiceConfig>>> getServiceConfigsForAllServices() {
	CatalogClient catClient = getClient();
	Map<String, List<String>> serviceToTags = catClient.getServices().getResponse();
	Map<String, Map<String, List<ServiceConfig>>> returnMap = new HashMap<>();
	for(Map.Entry<String, List<String>> entry : serviceToTags.entrySet()){
	    List<String> tags = entry.getValue();
	    String serviceName = entry.getKey();
	    Map<String, List<ServiceConfig>> serviceSpecificMap = new HashMap<>();
	    for(String tag : tags){
		List<ServiceConfig> nameAndTagSpecificConfigs = getServiceConfigsFor(serviceName, tag);
		serviceSpecificMap.put(tag, nameAndTagSpecificConfigs);
	    }
	    returnMap.put(serviceName, serviceSpecificMap);
	}
	return returnMap;
    }

    @Override
    public List<ServiceConfig> getServiceConfigsFor(String serviceName, String version) {
	List<CatalogService> catServices = getServices(serviceName, version);
	List<ServiceConfig> serviceConfigs = new ArrayList<>(catServices.size());
	for(CatalogService catService : catServices){
	    ServiceConfig svcConfig = new ConsulCatalogServiceConfigBuilder(catService).build();
	    serviceConfigs.add(svcConfig);
	}
	return serviceConfigs;
    }

    @Override
    public ServiceConfig getServiceConfigFor(String serviceName, String version) {
	List<ServiceConfig> configs = getServiceConfigsFor(serviceName, version);
	return getRandomElement(configs);
    }
}
