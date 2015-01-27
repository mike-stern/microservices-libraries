package gov.usgs.cida.microservices.api.messaging;

import java.util.Map;

public interface MessagingClient {
	
	/**
	 * 
	 * 
	 * @param topic
	 * @param requestId
	 * @param message
	 */
	public void sendMessage(String topic, String requestId, Map<String, String> message);
}
