package gov.usgs.cida.microservices.api.registration;

import gov.usgs.cida.microservices.config.ServiceConfig;

public interface RegistrationClient {
    public void registerService(ServiceConfig configuration);
    public void deregisterService(ServiceConfig configuration);
}
