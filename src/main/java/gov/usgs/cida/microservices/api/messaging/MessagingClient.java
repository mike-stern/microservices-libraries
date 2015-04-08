package gov.usgs.cida.microservices.api.messaging;

import java.util.Map;

public interface MessagingClient {
	
	/**
	 * 
	 * @param headers
	 * @param message
	 */
	public void sendMessage(Map<String, Object> headers, byte[] message);
	
	/**
	 * 
	 * @param requestId
	 * @param serviceRequestId
	 * @param e
	 */
	public void postError(String requestId, String serviceRequestId, Exception e);
}
