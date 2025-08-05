package com.optimal.solution.awssdkdemo.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.optimal.solution.awssdkdemo.Constants;
import com.optimal.solution.awsutils.S3Utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import software.amazon.awssdk.services.s3.S3Client;

@Controller
public class S3DemoController {

	Logger log = LoggerFactory.getLogger(S3DemoController.class);

	@GetMapping("/s3/bucket")
	public ModelAndView bucketDetails(@RequestParam String bucketname) {

		S3Client s3Client = S3Client.builder().region(Constants.REGION).build();

		S3Utils s3Utils = new S3Utils();
		String error=null;
		
		List<String> items = null;
		try {
			items = s3Utils.listItemsInBucket(s3Client, bucketname, null);
		} catch (Exception e) {
			log.error("Cannot list items in bucket",e);
			error = e.getMessage();
		}

		s3Client.close();
		ModelAndView result = new ModelAndView();
		result.setViewName("s3/itemslist");
		result.addObject("bucket", bucketname);
		result.addObject("items", items);
		result.addObject("error", error);
		return result;
	}

	@GetMapping("/s3/item")
	public ModelAndView itemDetails(@RequestParam String bucket, @RequestParam String item) {

		S3Client s3Client = S3Client.builder().region(Constants.REGION).build();

		S3Utils s3Utils = new S3Utils();
		s3Utils.getS3Object(s3Client, bucket, item);
		String fileData = s3Utils.getS3Object(s3Client, bucket, item);
		s3Client.close();
		
		//log.info(fileData);
		ModelAndView result = new ModelAndView();
		result.setViewName("s3/item");
		result.addObject("bucket", bucket);
		result.addObject("item", item);
		result.addObject("fileData", fileData);
		return result;
	}

	@RequestMapping(value = "/s3/uploadFile", method = RequestMethod.POST)
	public void submit(@RequestParam("file") MultipartFile file, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		log.info("inside uploadFIle");
		log.info("content type: " + file.getContentType());
		log.info("name: " + file.getOriginalFilename());

		File convertedFile = new File(file.getOriginalFilename());
		try {
			Files.copy(file.getInputStream(), convertedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		log.info("file to upload: " + convertedFile.getAbsolutePath());

		String bucket = request.getParameter("bucket");
		log.info("bucket:" + bucket);

		S3Client s3Client = S3Client.builder().region(Constants.REGION).build();
		S3Utils s3Utils = new S3Utils();

		try {
			s3Utils.uploadFile(s3Client, bucket, file.getOriginalFilename(), convertedFile.getAbsolutePath());
		} catch (Exception e) {
			log.error("Exception in upload file:", e);
		}
		s3Client.close();
		
		String url = "/s3/bucket?bucketname=" + bucket;
		log.info("going to " + url);
		response.sendRedirect(url);
	}

	@GetMapping("/s3/addnewbucket")
	public ModelAndView addnewbucket() {
		ModelAndView result = new ModelAndView();
		result.setViewName("s3/newbucket");
		return result;
	}

	@GetMapping("/s3/addnewfile")
	public ModelAndView addnewfile(@RequestParam String bucket) {
		ModelAndView result = new ModelAndView();
		result.addObject("bucket", bucket);
		result.setViewName("s3/news3file");
		return result;
	}

}
