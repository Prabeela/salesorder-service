package com.salesorder.microservices.controller;


import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.salesorder.microservices.domain.Customer;
import com.salesorder.microservices.domain.Item;
import com.salesorder.microservices.domain.SalesOrder;
import com.salesorder.microservices.repository.CustomerSOSRepository;
import com.salesorder.microservices.repository.SalesOrderRepository;
import com.salesorder.microservices.service.SalesOrderService;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

@Controller
public class SalesOrderController {
	
	 private Logger logger = LoggerFactory.getLogger(getClass());
	    
	    @Autowired
	    private SalesOrderService salesOrderService;
	    
	    @Autowired
		CustomerSOSRepository customerSOSRepository;
		@Autowired
		SalesOrderRepository salesOrderRepository;
		
		
		DBInfo dbinfo;
		
		public SalesOrderController(DBInfo dbinfo){
			this.dbinfo = dbinfo;
		}
		
		@GetMapping("/service3/dbinfo")
		public DBInfo getInfo(){
			return this.dbinfo;
		}

	    
	
	@PostMapping("/service3/orders")
	public ResponseEntity add(@RequestBody SalesOrder salesOrder) {
	
	
		logger.debug("finding item");
	   
	   // Item item = restTemplate.getForObject(fetchItemServiceUrl()+"/service2/item/pen", Item.class);
	
	    List<Item> itemList = salesOrderService.fetchItemDetails(salesOrder.getItems());
	    
	    Customer _customer=customerSOSRepository.findOne(salesOrder.getCust_id());
		
		logger.debug("(_customer.getId()))::::::::: "+_customer.getId());
	    
	    logger.debug("Adding item");
	   
	    if(_customer!=null && _customer.getId()!=null) {
			Integer price=salesOrderService.saveItemList(itemList,salesOrder);
			
			salesOrder.setPrice(price.toString());
			
			salesOrderRepository.saveSalesOrderDtls(salesOrder);

			return new ResponseEntity<List<Item>>(itemList, HttpStatus.OK);
		}
		else {
			logger.debug("Invalid customer id ::::::::: "+_customer.getId());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	    
	    //resolves to the greeting.vm velocity template
	    
	}
}	
	@Component
	class DBInfo {
		private String url;

		public DBInfo(DataSource dataSource) throws SQLException{
			this.url = dataSource.getConnection().getMetaData().getURL();
		}

		public String getUrl() {
			return url;
		}	
	

	}
