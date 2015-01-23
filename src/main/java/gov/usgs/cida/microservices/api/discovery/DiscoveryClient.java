package gov.usgs.cida.microservices.api.discovery;

import gov.usgs.cida.microservices.config.ServiceConfig;
import java.net.URI;
import java.util.List;
import java.util.List;
import java.util.Map;

public interface DiscoveryClient {

	/**
	 * Retrieves a map of known service names to a map of service versions
	 * to a List of URIs
	 * @return 
	 */
	public Map<String, Map<String, List<URI>>> getUrisForAllServices();
	
	/**
	 * Retrieves a map of known service names to a map of service versions
	 * to a List of ServiceConfigs
	 * @return 
	 */
	public Map<String, Map<String, List<ServiceConfig>>> getServiceConfigsForAllServices();
	
	/**
	 * Get a List of uris by service name and version
	 * 
	 * @param serviceName
	 * @param version
	 * @return 
	 */
	public List<URI> getUrisFor(String serviceName, String version);
	
	/**
	 * Get a List of ServiceConfigs by service name and version
	 * 
	 * @param serviceName
	 * @param version
	 * @return 
	 */
	public List<ServiceConfig> getServiceConfigsFor(String serviceName, String version);
	
	
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
