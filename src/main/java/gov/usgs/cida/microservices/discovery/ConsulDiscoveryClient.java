package gov.usgs.cida.microservices.discovery;

import com.orbitz.consul.CatalogClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.catalog.CatalogService;
import com.orbitz.consul.option.CatalogOptions;
import com.orbitz.consul.option.CatalogOptionsBuilder;
import gov.usgs.cida.microservices.api.discovery.DiscoveryClient;
import gov.usgs.cida.microservices.config.ConsulCatalogServiceConfigBuilder;
import gov.usgs.cida.microservices.config.ServiceConfig;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsulDiscoveryClient implements DiscoveryClient {

	private static final Logger logger = LoggerFactory.getLogger(ConsulDiscoveryClient.class);
	private final List<Consul> clients = new ArrayList<>();
	private static final Random random = new Random();
	public static final String CONSUL_NAME_VERSION_PATH_PREFIX = "/";
	public final static String CONSUL_NAME_VERSION_PATH_DELIM = "-";
	private void addClient(String ipAddress, int port){
		this.clients.add(Consul.newClient(ipAddress, port));
		logger.debug("Added consul client {}:{}", ipAddress, port);
	}

	private CatalogClient getCatalogClient() {
		return getRandomElement(clients).catalogClient();
	}

	private static <T> T getRandomElement(List<T> list) {
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
	 * Creates a client that communicates with many consul nodes on different IP
	 * addresses, but identical ports. Traffic is distributed to the nodes
	 * randomly.
	 *
	 * @param ipAddresses
	 * @param port
	 */
	public ConsulDiscoveryClient(Set<String> ipAddresses, int port) {
		for (String ipAddress : ipAddresses) {
			addClient(ipAddress, port);
		}
	}

	/**
	 * Creates a client that communicates with many consul nodes on different
	 * IP addresses, but identical ports. Traffic is distributed to the nodes
	 * randomly.
	 * 
	 * @param ipAddresses
	 * @param port 
	 */
	public ConsulDiscoveryClient(List<String> ipAddresses, int port){
		this(new HashSet<>(ipAddresses), port);
	}
	
	/**
	 * Creates a client that communicates with many consul nodes on different
	 * IP addresses, but identical ports. Traffic is distributed to the nodes
	 * randomly.
	 * 
	 * @param ipAddresses
	 * @param port 
	 */
	public ConsulDiscoveryClient(String[] ipAddresses, int port){
		this(Arrays.asList(ipAddresses), port);
	}
	
	/**
	 * Creates a client that communicates with many consul nodes on different
	 * IP addresses and different ports. Traffic is distributed to the nodes
	 * randomly.
	 *
	 * @param ipAddressToPort
	 */
	public ConsulDiscoveryClient(Map<String, Integer> ipAddressToPort) {
		for (Map.Entry<String, Integer> entry : ipAddressToPort.entrySet()) {
			addClient(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public Map<String, Map<String, Set<URI>>> getUrisForAllServices() {
		Map<String, Map<String, Set<URI>>> returnMap = new HashMap<>();
		Map<String, Map<String, Set<ServiceConfig>>> allServiceConfigs = getServiceConfigsForAllServices();
		for (Map.Entry<String, Map<String, Set<ServiceConfig>>> serviceToTagMap : allServiceConfigs.entrySet()) {
			String serviceName = serviceToTagMap.getKey();
			Map<String, Set<URI>> tagAndUriMap = new HashMap<>();
			Map<String, Set<ServiceConfig>> tagToServiceConfig = serviceToTagMap.getValue();
			for (Map.Entry<String, Set<ServiceConfig>> innerEntry : tagToServiceConfig.entrySet()) {
				String tag = innerEntry.getKey();
				//use a Set to ensure uniqueness
				Set<URI> tagSpecificUris = new HashSet<>();
				for (ServiceConfig serviceConfig : innerEntry.getValue()) {
					tagSpecificUris.add(buildServiceUri(serviceConfig));
				}
				tagAndUriMap.put(tag, tagSpecificUris);
			}
			returnMap.put(serviceName, tagAndUriMap);
		}
		return returnMap;
	}

	@Override
	public Set<URI> getUrisFor(String serviceName, String version) {
		Set<ServiceConfig> services = getServiceConfigsFor(serviceName, version);
		Set<URI> uris = new HashSet<>(services.size());
		for (ServiceConfig service : services) {
			uris.add(buildServiceUri(service));
		}
		return uris;
	}

	private List<CatalogService> getServices(String serviceName, String version) {
		CatalogClient catClient = getCatalogClient();
		CatalogOptions catOpts;
		catOpts = CatalogOptionsBuilder.builder().tag(version).build();
		logger.debug("Querying Consul for service '{}' at version '{}'", serviceName, version);
		List<CatalogService> services = catClient.getService(serviceName, catOpts).getResponse();
		return services;
	}

	private URI buildServiceUri(ServiceConfig svcConfig) {
		URI uri = null;
		if (null != svcConfig) {
			String address = svcConfig.getAddress();
			int port = svcConfig.getPort();
			URIBuilder uriBuilder = new URIBuilder();
			uriBuilder.setHost(address);
			uriBuilder.setPort(port);
			uriBuilder.setPath(CONSUL_NAME_VERSION_PATH_PREFIX + svcConfig.getName() + CONSUL_NAME_VERSION_PATH_DELIM + svcConfig.getVersion());
			try {
				uri = uriBuilder.build();
			} catch (URISyntaxException ex) {
				throw new RuntimeException("Error Building URI from information provided by consul", ex);
			}
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
	public Map<String, Map<String, Set<ServiceConfig>>> getServiceConfigsForAllServices() {
		CatalogClient catClient = getCatalogClient();
		logger.debug("Querying Consul for all services");
		Map<String, List<String>> serviceToTags = catClient.getServices().getResponse();
		logger.debug("Processing response from Consul");
		Map<String, Map<String, Set<ServiceConfig>>> returnMap = new HashMap<>();
		for (Map.Entry<String, List<String>> entry : serviceToTags.entrySet()) {
			List<String> tags = entry.getValue();
			String serviceName = entry.getKey();
			Map<String, Set<ServiceConfig>> serviceSpecificMap = new HashMap<>();
			for (String tag : tags) {
				Set<ServiceConfig> nameAndTagSpecificConfigs = getServiceConfigsFor(serviceName, tag);
				serviceSpecificMap.put(tag, nameAndTagSpecificConfigs);
			}
			returnMap.put(serviceName, serviceSpecificMap);
		}
		return returnMap;
	}

	@Override
	public Set<ServiceConfig> getServiceConfigsFor(String serviceName, String version) {
		List<CatalogService> catServices = getServices(serviceName, version);
		Set<ServiceConfig> serviceConfigs = new HashSet<>(catServices.size());
		for (CatalogService catService : catServices) {
			ServiceConfig svcConfig = new ConsulCatalogServiceConfigBuilder(catService).build();
			serviceConfigs.add(svcConfig);
		}
		return serviceConfigs;
	}

	@Override
	public ServiceConfig getServiceConfigFor(String serviceName, String version) {
		Set<ServiceConfig> configs = getServiceConfigsFor(serviceName, version);
		return configs.isEmpty() ? null : configs.iterator().next();
	}
}
