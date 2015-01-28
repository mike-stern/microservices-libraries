package gov.usgs.cida.microservices.api.discovery;

import gov.usgs.cida.microservices.config.ServiceConfig;
import java.net.URI;
import java.util.List;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DiscoveryClient {

	/**
	 * Retrieves a map of known service names to a map of service versions
	 * to a Set of URIs
	 * @return 
	 */
	public Map<String, Map<String, Set<URI>>> getUrisForAllServices();
	
	/**
	 * Retrieves a map of known service names to a map of service versions
	 * to a Set of ServiceConfigs
	 * @return 
	 */
	public Map<String, Map<String, Set<ServiceConfig>>> getServiceConfigsForAllServices();
	
	/**
	 * Get a List of uris by service name and version
	 * 
	 * @param serviceName
	 * @param version
	 * @return 
	 */
	public Set<URI> getUrisFor(String serviceName, String version);
	
	/**
	 * Get a List of ServiceConfigs by service name and version
	 * 
	 * @param serviceName
	 * @param version
	 * @return 
	 */
	public Set<ServiceConfig> getServiceConfigsFor(String serviceName, String version);
	
	
	/**
	 * Get a single URI by service name and version.
	 * This interface allows implementations to specify which URI 
	 * to return if multiple URIs are available.
	 * 
	 * @param serviceName
	 * @param version
	 * @return 
	 */
	public URI getUriFor(String serviceName, String version);
	
	/**
	 * Get a single ServiceConfig by service name and version.
	 * This interface allows implementations to specify which ServiceConfig 
	 * to return if multiple ServiceConfigs are available.
	 * 
	 * @param serviceName
	 * @param version
	 * @return 
	 */
	public ServiceConfig getServiceConfigFor(String serviceName, String version);
	
}
