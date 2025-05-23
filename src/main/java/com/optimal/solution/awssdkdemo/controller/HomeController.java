package com.optimal.solution.awssdkdemo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.optimal.solution.awssdkdemo.Constants;
import com.optimal.solutions.awsutils.S3Utils;

import software.amazon.awssdk.services.s3.S3Client;

@Controller
public class HomeController {

	@GetMapping("/")
	public ModelAndView index() {
		ModelAndView result = new ModelAndView();
		result.setViewName("index");
		return result;
	}


	@GetMapping("/s3buckets")
	public ModelAndView s3demo() {

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

}