package gov.usgs.cida.microservices.api.discovery;

import java.util.List;
import java.util.Map;

public interface CatalogClient {

	/**
	 * Registers a service into the catalog
	 *
	 * @param serviceId
	 * @param serviceName
	 */
	public void registerService(String serviceId, String serviceName, Integer servicePort);
	
	public List<Map<String, String>> getCatalogService(String serviceName, Map<String, String> params, String tag);
	
	public Map<String, String> getServiceByName(String serviceName);
}
