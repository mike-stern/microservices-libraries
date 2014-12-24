package gov.usgs.cida.microservices.catalog;

import com.ecwid.consul.v1.ConsistencyMode;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.agent.model.Service;
import com.ecwid.consul.v1.catalog.model.CatalogService;

import gov.usgs.cida.microservices.api.discovery.CatalogClient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class ConsulCatalogClient implements CatalogClient {

	private String agentHost = "127.0.0.1";
	private Integer agentPort = 8500;

	public ConsulCatalogClient() {
		super();
	}
	
	public ConsulCatalogClient(String agentHost, Integer agentPort) {
		if (StringUtils.isNotBlank(agentHost)) {
			this.agentHost = agentHost;
		}

		if (agentPort != null) {
			this.agentPort = agentPort;
		}
	}

	@Override
	public void registerService(String serviceId, String serviceName, Integer servicePort) {
		ConsulClient client = new ConsulClient(this.agentHost, agentPort);

		NewService newService = new NewService();
		newService.setId(serviceId);
		newService.setName(serviceName);
		newService.setPort(servicePort);
		client.agentServiceRegister(newService);
	}

	@Override
	public List<Map<String, String>> getCatalogService(String serviceName, Map<String, String> params, String tag) {
		List<Map<String, String>> response = new ArrayList<>();
		Response<List<CatalogService>> catalogServiceListResponse;
		ConsulClient client = new ConsulClient(this.agentHost, this.agentPort);

		if (StringUtils.isNotBlank(tag)) {
			catalogServiceListResponse = client.getCatalogService(serviceName, tag, new QueryParams(ConsistencyMode.DEFAULT));
		} else {
			catalogServiceListResponse = client.getCatalogService(serviceName, new QueryParams(ConsistencyMode.DEFAULT));
		}

		List<CatalogService> catalogServiceList = catalogServiceListResponse.getValue();
		for (CatalogService service : catalogServiceList) {
			Map<String, String> serviceMap = new HashMap<>();
			serviceMap.put("address", service.getAddress());
			serviceMap.put("node", service.getNode());
			serviceMap.put("serviceId", service.getServiceId());
			serviceMap.put("servicePort", Integer.toString(service.getServicePort(), 10));
			serviceMap.put("serviceName", service.getServiceName());
			serviceMap.put("serviceTags", Arrays.toString(service.getServiceTags().toArray(new String[0])));
			response.add(serviceMap);
		}
		return response;
	}

	@Override
	public Map<String, String> getServiceByName(String serviceName) {
		ConsulClient client = new ConsulClient(this.agentHost, this.agentPort);
		Map<String, String> response = new HashMap<>();
		Response<Map<String, Service>> agentServices = client.getAgentServices();
		Map<String, Service> servicesByName = agentServices.getValue();
		if (servicesByName.containsKey(serviceName)) {
			Service svc = servicesByName.get(serviceName);
			response.put("id", svc.getId());
			response.put("service", svc.getService());
			response.put("port", Integer.toUnsignedString(svc.getPort()));
			response.put("tags", svc.getTags() != null ? Arrays.toString(svc.getTags().toArray(new String[0])) : "");
		}
		return response;
	}
}
