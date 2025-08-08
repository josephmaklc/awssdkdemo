package com.optimal.solution.awssdkdemo.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.optimal.solution.awssdkdemo.Constants;
import com.optimal.solution.awsutils.SqsUtils;

import software.amazon.awssdk.services.sqs.SqsClient;

@Controller
public class SqsDemoController {

	Logger log = LoggerFactory.getLogger(SqsDemoController.class);

	@GetMapping("/sqs/addqueue")
	public ModelAndView addnewbucket() {
		ModelAndView result = new ModelAndView();
		result.setViewName("sqs/newqueue");
		return result;
	}

	@GetMapping("/sqs/queue")
	public ModelAndView getTopic(@RequestParam String queue) {
		log.info("getQueue " + queue);
		SqsClient client = SqsClient.builder().region(Constants.REGION).build();

		String error = "";
		SqsUtils sqsUtils = new SqsUtils();
		Map<String, String> attributes =  null;
		try {
			 attributes = sqsUtils.getQueueAttributes(client, queue);
			client.close();
		} catch (Exception e) {
			log.error("Cannot get function details", e);
			error = e.getMessage();
		}

		ModelAndView result = new ModelAndView();
		result.setViewName("sqs/queue");
		result.addObject("queue", queue);
		result.addObject("attributes", attributes);
		result.addObject("error", error);
		return result;
	}


}
