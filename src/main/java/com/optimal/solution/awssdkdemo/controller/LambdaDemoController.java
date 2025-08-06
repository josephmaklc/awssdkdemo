package com.optimal.solution.awssdkdemo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.optimal.solution.awssdkdemo.Constants;
import com.optimal.solution.awsutils.LambdaUtils;

import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration;

@Controller
public class LambdaDemoController {

	Logger log = LoggerFactory.getLogger(LambdaDemoController.class);
	
	@GetMapping("/lambda/function")
	public ModelAndView getFunction(@RequestParam String functionName) {
		log.info("getFunction "+functionName);
		LambdaClient client = LambdaClient.builder().region(Constants.REGION).build();

		FunctionConfiguration config = null;
		LambdaUtils lambdaUtils = new LambdaUtils();
		String error = null;
		try {
			config = lambdaUtils.getFunctionDetails(client, functionName);
			client.close();
		} catch (Exception e) {
			log.error("Cannot get function details",e);
			error = e.getMessage();
		}

		ModelAndView result = new ModelAndView();
		result.setViewName("lambda/function");
		result.addObject("functionName", functionName);
		result.addObject("config", config);
		result.addObject("error", error);
		return result;
	}

}
