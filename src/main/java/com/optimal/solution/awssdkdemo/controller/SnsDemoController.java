package com.optimal.solution.awssdkdemo.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.optimal.solution.awssdkdemo.Constants;
import com.optimal.solution.awsutils.SnsUtils;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.Subscription;

@Controller
public class SnsDemoController {

	Logger log = LoggerFactory.getLogger(SnsDemoController.class);

	@GetMapping("/sns/addtopic")
	public ModelAndView addnewbucket() {
		ModelAndView result = new ModelAndView();
		result.setViewName("sns/newtopic");
		return result;
	}

	@GetMapping("/sns/topic")
	public ModelAndView getTopic(@RequestParam String topicArn) {
		log.info("getTopic " + topicArn);
		SnsClient client = SnsClient.builder().region(Constants.REGION).build();

		String error = "";
		SnsUtils snsUtils = new SnsUtils();
		Map<String, String> attributes =  null;
		List<Subscription> subscriptions = null;
		try {
			 attributes = snsUtils.getTopicAttributes(client, topicArn);

//			System.out.println("Attributes for Topic ARN: " + topicArn);
//			for (Map.Entry<String, String> entry : attributes.entrySet()) {
//				System.out.println("  " + entry.getKey() + ": " + entry.getValue());
//			}
			 subscriptions = snsUtils.listSNSSubscriptionsForTopic(client,topicArn);
			client.close();
		} catch (Exception e) {
			log.error("Cannot get function details", e);
			error = e.getMessage();
		}

		ModelAndView result = new ModelAndView();
		result.setViewName("sns/topic");
		result.addObject("topicArn", topicArn);
		result.addObject("attributes", attributes);
		result.addObject("subscriptions", subscriptions);
		result.addObject("error", error);
		return result;
	}

	@GetMapping("/sns/addsubscriber")
	public ModelAndView addsubscriber(@RequestParam String topicArn) {
		ModelAndView result = new ModelAndView();
		result.setViewName("sns/newsubscriber");
		result.addObject("topicArn", topicArn);
		return result;
	}
}
