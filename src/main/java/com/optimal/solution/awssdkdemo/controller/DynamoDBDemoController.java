package com.optimal.solution.awssdkdemo.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.optimal.solution.awssdkdemo.Constants;
import com.optimal.solutions.awsutils.DynamoUtils;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;

@Controller

public class DynamoDBDemoController {
	Logger log = LoggerFactory.getLogger(DynamoDBDemoController.class);

	@GetMapping("/dynamo/describeTable")
	public ModelAndView bucketDetails(@RequestParam String tableName) {

		DynamoDbClient dynamoDbClient = DynamoDbClient.builder().region(Constants.REGION).build();

		DynamoUtils dynamoUtils = new DynamoUtils();
		
		TableDescription tableInfo = null;
		String error=null;
		try {
			tableInfo = dynamoUtils.describeDymamoDBTable(dynamoDbClient, tableName);
		} catch (Exception e) {
			error = e.getMessage();
			log.error("Cannot get table description",e);
		}

		dynamoDbClient.close();
		
		ModelAndView result = new ModelAndView();
		result.setViewName("dynamo/table");
		result.addObject("tableInfo", tableInfo);
		result.addObject("error", error);
		return result;
	}
	
	@GetMapping("/dynamo/scanTable")
	public ModelAndView scanTable(@RequestParam String tableName) {

		log.info("scantable: "+tableName);
		DynamoDbClient dynamoDbClient = DynamoDbClient.builder().region(Constants.REGION).build();

		DynamoUtils dynamoUtils = new DynamoUtils();
		
		
		String error=null;
		List<Map<String, AttributeValue>> items = new ArrayList<Map<String,AttributeValue>>();
		try {
		 items = dynamoUtils.scanTable(dynamoDbClient, tableName);
			 
		} catch (Exception e) {
			error = e.getMessage();
			log.error("Cannot scan table",e);
		}

		dynamoDbClient.close();
		
		ModelAndView result = new ModelAndView();
		result.setViewName("dynamo/tablecontent");
		result.addObject("tableName",tableName);
		result.addObject("items",items);
		result.addObject("error", error);
		return result;
	}
	
	@GetMapping("/dynamo/queryTable")
	public ModelAndView queryTable(@RequestParam String tableName) {

		DynamoDbClient dynamoDbClient = DynamoDbClient.builder().region(Constants.REGION).build();

		DynamoUtils dynamoUtils = new DynamoUtils();
		
		TableDescription tableInfo = null;
		String error=null;
		try {
			tableInfo = dynamoUtils.describeDymamoDBTable(dynamoDbClient, tableName);
		} catch (Exception e) {
			error = e.getMessage();
			log.error("Cannot get table description",e);
		}

		dynamoDbClient.close();
		
		ModelAndView result = new ModelAndView();
		result.setViewName("dynamo/querytable");
		result.addObject("tableInfo", tableInfo);
		result.addObject("error", error);
		return result;
	}
	
	@GetMapping("/dynamo/addnewtable")
	public ModelAndView addnewtable() {

		ModelAndView result = new ModelAndView();
		result.setViewName("dynamo/newtable");

		return result;
	}
	
	@GetMapping("/dynamo/addnewentry")
	public ModelAndView addnewentry(@RequestParam String tableName) {

		DynamoDbClient dynamoDbClient = DynamoDbClient.builder().region(Constants.REGION).build();
		DynamoUtils dynamoUtils = new DynamoUtils();
		TableDescription tableInfo = null;
		String error=null;
		try {
			tableInfo = dynamoUtils.describeDymamoDBTable(dynamoDbClient, tableName);
		} catch (Exception e) {
			error = e.getMessage();
			log.error("Cannot get table description",e);
		}
		dynamoDbClient.close();
		
		ModelAndView result = new ModelAndView();
		result.setViewName("dynamo/newentry");
		result.addObject("tableName",tableName);
		result.addObject("tableInfo",tableInfo);
		result.addObject("error",error);
		
		return result;
	}
}
