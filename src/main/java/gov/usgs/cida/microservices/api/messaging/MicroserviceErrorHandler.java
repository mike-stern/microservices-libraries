package gov.usgs.cida.microservices.api.messaging;

/**
 *
 * @author thongsav
 */
public interface MicroserviceErrorHandler {
	public void handleError(String requestId, String serviceRequestId, String errorType, String errorMessage);
}
