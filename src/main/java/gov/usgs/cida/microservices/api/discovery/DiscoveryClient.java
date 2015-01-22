package gov.usgs.cida.microservices.api.discovery;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface DiscoveryClient {

	/**
	 * Retrieves a map of known service names to a map of service versions
	 * to a Collection of URIs
	 * @return 
	 */
	public Map<String, Map<String, Collection<URI>>> getServices();
	
	/**
	 * Get a collection of uris by service name and version
	 * 
	 * @param serviceName
	 * @param version
	 * @return 
	 */
	public Collection<URI> getUrisFor(String serviceName, String version);
	
	
	/**
	 * Get a single URI by service name and version.
	 * This interface does not specify which uri to return if multiple URIs
	 * are available.
	 * 
	 * @param serviceName
	 * @param version
	 * @return 
	 */
	public URI getUriFor(String serviceName, String version);
}
