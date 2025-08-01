package com.optimal.solution.awssdkdemo.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.optimal.solution.awssdkdemo.model.DynamoDbJson;
import com.optimal.solution.awssdkdemo.model.DynamoDbSearchParam;
import com.optimal.solution.awssdkdemo.model.DynamoTableIdentitifer;
import com.optimal.solution.awssdkdemo.model.S3ItemIdentifier;
import com.optimal.solutions.awsutils.DynamoUtils;
import com.optimal.solutions.awsutils.S3Utils;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.s3.S3Client;

@RestController
public class APIController {

	Logger log = LoggerFactory.getLogger(S3DemoController.class);

	Region region = Region.US_EAST_1;

	@PostMapping("/s3/deleteItem")
	public String deleteS3Item(@RequestBody S3ItemIdentifier request) {
		log.info("delete s3 item: bucket:"+request.getBucketName() + " itemKey:" + request.getItemKey());

		S3Client s3Client = S3Client.builder().region(region).build();

		S3Utils s3Utils = new S3Utils();
		try { 
			s3Utils.deleteFileS3(s3Client, request.getBucketName(), request.getItemKey());
		} catch (Exception e) {
			return e.getMessage();
		}
		return "SUCCESS";
	}

	@PostMapping("/s3/deleteBucket")
	public String deleteS3Bucket(@RequestBody S3ItemIdentifier request) {
		log.info("delete bucket: "+request.getBucketName());

		S3Client s3Client = S3Client.builder().region(region).build();

		S3Utils s3Utils = new S3Utils();
		try {
			s3Utils.deleteBucket(s3Client, request.getBucketName());
		} catch (Exception e) {
			return e.getMessage();
		}
		return "SUCCESS";

	}

	@PostMapping("/s3/addBucket")
	public String addS3Bucket(@RequestBody S3ItemIdentifier request) {
		log.info("add new bucket: "+request.getBucketName());

		S3Client s3Client = S3Client.builder().region(region).build();

		S3Utils s3Utils = new S3Utils();
		try {
			s3Utils.createBucket(s3Client, request.getBucketName());
		} catch (Exception e) {
			return e.getMessage();
		}
		return "SUCCESS";

	}

	@PostMapping("/dynamo/deleteTable")
	public String deleteDynamoTable(@RequestBody DynamoTableIdentitifer request) {
		log.info("delete table: "+request.getTableName());

		DynamoDbClient ddb = DynamoDbClient.builder().region(region).build();

		DynamoUtils dynamoUtils = new DynamoUtils();
		try {
			dynamoUtils.deleteTable(ddb, request.getTableName());
		} catch (Exception e) {
			return e.getMessage();
		}
		return "SUCCESS";

	}
	
	@PostMapping("/dynamo/addTable")
	public String addNewTable(@RequestBody DynamoTableIdentitifer request) {
		log.info("add table: "+request.getTableName());
		log.info("key: "+request.getPrimaryKey());
		log.info("key type: "+request.getPrimaryKeyType());
		log.info("sort key: "+request.getSortKey());
		log.info("sort key type: "+request.getSortKeyType());

		DynamoDbClient ddb = DynamoDbClient.builder().region(region).build();

		DynamoUtils dynamoUtils = new DynamoUtils();
		try {
			dynamoUtils.createTable(ddb, request.getTableName(), request.getPrimaryKey(), request.getPrimaryKeyType(), request.getSortKey(),
					request.getSortKeyType());
		} catch (Exception e) {
			return e.getMessage();
		}
		return "SUCCESS";

	}
	
	@PostMapping("/dynamo/addEntry")
	public String addNewEntry(@RequestBody DynamoDbJson request) {
		log.info("add table: "+request.getTableName());
		log.info("json: "+request.getJson());

		DynamoDbClient ddb = DynamoDbClient.builder().region(region).build();

		DynamoUtils dynamoUtils = new DynamoUtils();
		try {
			dynamoUtils.putJSONItemInTable(ddb, request.getTableName(), request.getJson());
		} catch (Exception e) {
			return e.getMessage();
		}
		return "SUCCESS";

	}
	
	@PostMapping("/dynamo/query")
	public  List<String> dynamoQuery(@RequestBody DynamoDbSearchParam request) {
		log.info("query table: "+request.getTableName());
		log.info("pk name: "+request.getPrimaryKeyName());
		log.info("pk type: "+request.getPrimaryKeyType());
		log.info("pk value: "+request.getPrimaryKeyValue());
		log.info("sk name: "+request.getSortKeyName());
		log.info("sk type: "+request.getSortKeyType());
		log.info("sk value: "+request.getSortKeyValue());

		DynamoDbClient ddb = DynamoDbClient.builder().region(region).build();

		DynamoUtils dynamoUtils = new DynamoUtils();
		try {
			 List<Map<String, AttributeValue>> items = dynamoUtils.queryItemCompositeKey(ddb, request.getTableName(), 
					request.getPrimaryKeyName(), request.getPrimaryKeyType(), request.getPrimaryKeyValue(), 
					request.getSortKeyName(), request.getSortKeyType(), request.getSortKeyValue() );
			 
			 List<String> result = new ArrayList<String>();
			 if (items!=null) {
			 items.forEach(item -> {
					log.info("Item found: " + item);
					result.add(item.toString());
				});
			 }
			 return result;
		} catch (Exception e) {
			log.error("Error in query dynamo",e);
			return null;
		}

	}
}
