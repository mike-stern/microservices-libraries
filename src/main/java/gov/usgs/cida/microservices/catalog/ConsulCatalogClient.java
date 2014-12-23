package gov.usgs.cida.microservices.catalog;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;

import gov.usgs.cida.microservices.api.catalog.CatalogClient;

public class ConsulCatalogClient implements CatalogClient {

	@Override
	public void register(String catalogHost, String serviceId,
			String serviceName, int servicePort) {
		String host = catalogHost != null ? catalogHost :  "localhost";
		ConsulClient client = new ConsulClient(host);
		
		// register new service
		NewService newService = new NewService();
		newService.setId(serviceId);
		newService.setName(serviceName);
		newService.setPort(servicePort);
		client.agentServiceRegister(newService);
	}
}
