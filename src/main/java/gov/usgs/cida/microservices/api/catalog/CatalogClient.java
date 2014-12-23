package gov.usgs.cida.microservices.api.catalog;

public interface CatalogClient {
	/**
	 * 
	 * @param catalogHost
	 * @param serviceId
	 * @param serviceName
	 * @param servicePort
	 */
	public void register(String catalogHost, String serviceId, String serviceName, int servicePort);
}
