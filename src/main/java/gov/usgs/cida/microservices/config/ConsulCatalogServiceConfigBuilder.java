package gov.usgs.cida.microservices.config;

import com.orbitz.consul.model.catalog.CatalogService;


public class ConsulCatalogServiceConfigBuilder extends ServiceConfigBuilder{

    public ConsulCatalogServiceConfigBuilder(CatalogService catService){
	this.setAddress(catService.getAddress())
		.setId(catService.getServiceId())
		.setName(catService.getServiceName())
		.setNode(catService.getNode())
		.setPort(catService.getServicePort())
		.setTags(catService.getServiceTags().toArray(new String[0]));		
    }
    
}
