package gov.usgs.cida.microservices.api.messaging;

import java.util.Map;

/**
 *
 * @author dmsibley
 */
public interface MicroserviceHandler {
	public void handle(Map<String, String> params);
	public Iterable<Map<String, Object>> getBindings(String serviceName); 
}
