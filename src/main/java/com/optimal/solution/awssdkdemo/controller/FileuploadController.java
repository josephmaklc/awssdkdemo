package com.optimal.solution.awssdkdemo.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class FileuploadController {

	Logger log = LoggerFactory.getLogger(FileuploadController.class);


	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	public ModelAndView submit(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
		log.info("inside uploadFIle");
		log.info("content type: " + file.getContentType());
		log.info("name: " + file.getOriginalFilename());

		File convertedFile = new File(file.getOriginalFilename());
        try {
            Files.copy(file.getInputStream(), convertedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("file to upload: "+convertedFile.getAbsolutePath());
        
		ModelAndView result = new ModelAndView();
		result.addObject("file", file);
		result.setViewName("fileUploadView");
		return result;
	}

}
