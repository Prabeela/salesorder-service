package com.salesorder.microservices.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.salesorder.microservices.domain.Item;
import com.salesorder.microservices.domain.SalesOrder;
import com.salesorder.microservices.repository.SalesOrderRepository;
import com.salesorder.microservices.service.SalesOrderService;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;

import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RefreshScope
public class SalesOrderService {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
//	@Autowired
//	private DiscoveryClient discoveryClient;
	
	
	
	 private EurekaClient discoveryClient;
	 private RestTemplate restTemplate;
	    
	  
	 
	 @Autowired
	  private LoadBalancerClient loadBalancerClient;
	
	@Autowired
	public SalesOrderService(EurekaClient discoveryClient) {
		//this.discoveryClient = discoveryClient;
		 	this.discoveryClient = discoveryClient;
	        this.restTemplate = new RestTemplate();
	}
	
	
	@Value("${itemServiceAppName}")
	private String itemServiceAppName;

	@Value("${customerServiceAppName}")
	private String customerServiceAppName;


	@Autowired
	SalesOrderRepository salesOrderRepository;
	

	
	public Integer saveItemList(List<Item> itemList,SalesOrder salesOrder) {
		
		Integer price=0;
		
		for (int i = 0; i < itemList.size(); i++) {
			
		
			if(salesOrder.getItems().get(i).equals(itemList.get(i).getName())) {
					price=price+salesOrder.getQuantity().get(i)*Integer.parseInt(itemList.get(i).getPrice());
					
					
					salesOrderRepository.saveOrderLineItemDtls(salesOrder,itemList.get(i),salesOrder.getQuantity().get(i).toString());
			}
			else {
				for(int j=i+1;j<salesOrder.getItems().size();j++) {
					
				
					if(salesOrder.getItems().get(j).equals(itemList.get(i).getName()))
						price=price+salesOrder.getQuantity().get(j)*Integer.parseInt(itemList.get(i).getPrice());
					
						salesOrderRepository.saveOrderLineItemDtls(salesOrder,itemList.get(i),salesOrder.getQuantity().get(j).toString());
				}
			}
		}
		return price;
	}
	
	
	
	
	
	public List<Item> fetchItemDetails(ArrayList<String> itemNameList) {
		 
		//String baseUrl = fetchServiceUrl(itemServiceAppName);

		logger.debug("Fetching details of item::" + itemNameList.size());

		List<Item> itemList = new ArrayList<Item>();
		for (int i = 0; i < itemNameList.size(); i++) {
			logger.debug("Fetching details of item::" + itemNameList.get(i));
			
			String baseUrl=fetchItemServiceUrl(itemServiceAppName);
			logger.debug("baseUrl "+baseUrl);
			
			Item item=getItem(baseUrl,itemNameList.get(i));
			if(item!=null) {
				itemList.add(item);
			}
			//String itemUrl = baseUrl + "/service2/item/" + itemNameList.get(i);
			//logger.debug("item url is ::"+itemUrl);
			//itemList.add(getItem(itemUrl));	

			//RestTemplate restTemplate = new RestTemplate();
			
		}
		return itemList;
	}
	

	
	
	private String fetchItemServiceUrl(String itemServiceAppName) {
	   // InstanceInfo instance = discoveryClient.getNextServerFromEureka(itemServiceAppName, false);
	    
		 ServiceInstance instance = loadBalancerClient.choose(itemServiceAppName);
		//logger.debug("instanceID: {}", instance.getId());
	
	    //String itemServiceUrl = instance.getHomePageUrl();
	    //logger.debug("itemServiceUrl service homePageUrl: {}", itemServiceUrl);
	    
	    logger.debug("uri: {}", instance.getUri().toString());
	    logger.debug("serviceId: {}", instance.getServiceId());

	    return instance.getUri().toString();
	
	   // return itemServiceUrl;
	}
	
	 @HystrixCommand(fallbackMethod = "defaultItem")
	public Item getItem(String baseUrl,String itemName) {
		
		Item item = null;
		try {
			
			
			 ResponseEntity<Item> responseEntity = restTemplate.getForEntity(baseUrl+"/service2/item/"+itemName, Item.class);
			    
			
			if(responseEntity!=null && responseEntity.hasBody() && responseEntity.getBody() != null) {
				logger.debug("Item added");
				item=responseEntity.getBody();
			}
			
		}
		catch(HttpServerErrorException errorException) {
	           String responseBody = errorException.getResponseBodyAsString();
	           logger.debug("error is:: "+responseBody);
	           // You can use this string to create MyOutput pojo using ObjectMapper.
	     }
		catch (HttpStatusCodeException exception) {
				String responseBody = exception.getResponseBodyAsString();
	           logger.debug("error is:: "+responseBody);
		}
		catch(RestClientException e){
		     //no response payload, tell the user sth else 
			String responseBody = e.getMessage();
	           logger.debug("error is:: "+responseBody);
		}
		catch(Exception e) {
			String responseBody = e.getMessage();
	           logger.debug("error is:: "+responseBody);
			
		}
		return item;

	}
	 public Item defaultItem(String baseUrl,String itemName) {
		    logger.debug("Default Item used.");
		    Item item = new Item();
		    item.setName("default-item");
		    item.setDescription("defualt-item-description");
		    return item;
		  }

	private static HttpEntity<?> getHeaders() throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		return new HttpEntity<>(headers);
	}

	
}
