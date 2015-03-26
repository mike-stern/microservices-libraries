package gov.usgs.cida.microservices.api.messaging;

import java.io.IOException;
import java.util.Map;

/**
 *
 * @author dmsibley
 */
public interface MicroserviceHandler {
	public void handle(Map<String, Object> params, byte[] body) throws IOException;
	public Iterable<Map<String, Object>> getBindings(String serviceName); 
}
