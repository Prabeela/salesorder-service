package com.salesorder.microservices;


import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.salesorder.microservices.domain.Item;
import com.salesorder.microservices.domain.SalesOrder;
import com.salesorder.microservices.service.SalesOrderService;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

@Controller
public class SalesOrderController {
	
	 private Logger logger = LoggerFactory.getLogger(getClass());
	    
	    @Autowired
	    private SalesOrderService salesOrderService;

	    
	
	@PostMapping("/service3/orders")
	public ResponseEntity add(@RequestBody SalesOrder salesOrder) {
	
	
		logger.debug("finding item");
	   
	   // Item item = restTemplate.getForObject(fetchItemServiceUrl()+"/service2/item/pen", Item.class);
	
	    List<Item> itemList = salesOrderService.fetchItemDetails(salesOrder.getItems());
	    
	    logger.debug("Adding item");
	   
	
	    //resolves to the greeting.vm velocity template
	    return new ResponseEntity<List<Item>>(itemList, HttpStatus.OK);
	}
	
	
	

}
