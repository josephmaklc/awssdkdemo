package com.optimal.solution.awssdkdemo.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.optimal.solution.awssdkdemo.Constants;
import com.optimal.solution.awsutils.DynamoUtils;
import com.optimal.solution.awsutils.LambdaUtils;
import com.optimal.solution.awsutils.S3Utils;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.s3.S3Client;

@Controller
public class HomeController {

	Logger log = LoggerFactory.getLogger(HomeController.class);
	
	@GetMapping("/")
	public ModelAndView index() {
		ModelAndView result = new ModelAndView();
		result.setViewName("index");
		return result;
	}


	@GetMapping("/s3buckets")
	public ModelAndView s3Demo() {

		S3Client s3Client = S3Client.builder().region(Constants.REGION).build();

		S3Utils s3Utils = new S3Utils();
		List<String> buckets = new ArrayList<>();
		String message = "";
		try {
			buckets = s3Utils.listBuckets(s3Client);
		} catch (Exception e) {
			message = e.getMessage();
		}
		s3Client.close();
		ModelAndView result = new ModelAndView();
		result.setViewName("s3/buckets");
		result.addObject("buckets", buckets);
		result.addObject("error", message);
		return result;

	}

	@GetMapping("/dynamo")
	public ModelAndView dynamoDemo() {
		log.info("listing all dynamo tables");
		DynamoDbClient dynamoDbClient = DynamoDbClient.builder().region(Constants.REGION).build();

		DynamoUtils dbUtils = new DynamoUtils();
		String message = "";
		List<String> tables = new ArrayList<>();
		try {
			tables = dbUtils.listAllTables(dynamoDbClient);
		} catch (Exception e) {
			message = e.getMessage();
		}
		dynamoDbClient.close();
		
		ModelAndView result = new ModelAndView();
		result.setViewName("dynamo/tables");
		result.addObject("tables", tables);
		result.addObject("error", message);
		return result;

	}

	@GetMapping("/lambda")
	public ModelAndView lambdaDemo() {
		log.info("listing all lambda functions");
		LambdaClient lambdaClient = LambdaClient.builder().region(Constants.REGION).build();

		LambdaUtils lambdaUtils = new LambdaUtils();
		String message = "";
		List<String> functions = new ArrayList<>();
		try {
			functions = lambdaUtils.listFunctions(lambdaClient);
		} catch (Exception e) {
			message = e.getMessage();
		}
		lambdaClient.close();
		
		ModelAndView result = new ModelAndView();
		result.setViewName("lambda/functions");
		result.addObject("functions", functions);
		result.addObject("error", message);
		return result;

	}
}