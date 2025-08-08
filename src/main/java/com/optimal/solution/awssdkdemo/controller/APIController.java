package com.optimal.solution.awssdkdemo.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.optimal.solution.awssdkdemo.Constants;
import com.optimal.solution.awssdkdemo.model.DynamoDbJson;
import com.optimal.solution.awssdkdemo.model.DynamoDbSearchParam;
import com.optimal.solution.awssdkdemo.model.DynamoTableIdentitifer;
import com.optimal.solution.awssdkdemo.model.LambdaFunctionParam;
import com.optimal.solution.awssdkdemo.model.S3ItemIdentifier;
import com.optimal.solution.awssdkdemo.model.SNSMessageParam;
import com.optimal.solution.awssdkdemo.model.SNSSubscriberParam;
import com.optimal.solution.awssdkdemo.model.SNSTopicParam;
import com.optimal.solution.awssdkdemo.model.SQSQueueParam;
import com.optimal.solution.awsutils.DynamoUtils;
import com.optimal.solution.awsutils.LambdaUtils;
import com.optimal.solution.awsutils.S3Utils;
import com.optimal.solution.awsutils.SnsUtils;
import com.optimal.solution.awsutils.SqsUtils;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * This class contains ajax calls from the front end
 */
@RestController
public class APIController {

	Logger log = LoggerFactory.getLogger(APIController.class);

	Region region = Constants.REGION;

	@PostMapping("/s3/deleteItem")
	public String deleteS3Item(@RequestBody S3ItemIdentifier request) {
		log.info("delete s3 item: bucket:"+request.getBucketName() + " itemKey:" + request.getItemKey());

		S3Client s3Client = S3Client.builder().region(region).build();

		S3Utils s3Utils = new S3Utils();
		try { 
			s3Utils.deleteFileS3(s3Client, request.getBucketName(), request.getItemKey());
			s3Client.close();
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
			s3Client.close();
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
			s3Client.close();
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
			ddb.close();
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
			ddb.close();
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
			ddb.close();
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
			 ddb.close();
			 return result;
		} catch (Exception e) {
			log.error("Error in query dynamo",e);
			return null;
		}

	}
	
	@PostMapping("/lambda/deleteFunction")
	public String deleteLambdaFunction(@RequestBody LambdaFunctionParam request) {
		log.info("delete lambda: "+request.getFunctionName());

		LambdaClient client = LambdaClient.builder().region(region).build();

		LambdaUtils lambdaUtils = new LambdaUtils();
		try {
			lambdaUtils.deleteFunction(client, request.getFunctionName());
			client.close();
		} catch (Exception e) {
			return e.getMessage();
		}
		return "SUCCESS";
	}

	@PostMapping("/lambda/invokeFunction")
	public String invokeFunction(@RequestBody LambdaFunctionParam request) {
		log.info("invoke lambda: "+request.getFunctionName());
		String result="";
		LambdaClient client = LambdaClient.builder().region(region).build();

		LambdaUtils lambdaUtils = new LambdaUtils();
		try {
			result = lambdaUtils.invokeFunction(client, request.getFunctionName(), request.getPayload());
			client.close();
		} catch (Exception e) {
			return e.getMessage();
		}
		return result;
	}
	
	@PostMapping("/sns/deleteTopic")
	public String deleteSNSTopic(@RequestBody SNSTopicParam request) {
		log.info("delete topic: "+request.getArn());

		SnsClient client = SnsClient.builder().region(region).build();

		SnsUtils snsUtils = new SnsUtils();
		try {
			snsUtils.deleteSNSTopic(client, request.getArn());
			client.close();
		} catch (Exception e) {
			return e.getMessage();
		}
		return "SUCCESS";
	}

	@PostMapping("/sns/addTopic")
	public String addSNSTopic(@RequestBody SNSTopicParam request) {
		log.info("add topic: "+request.getTopicName());

		SnsClient client = SnsClient.builder().region(region).build();

		SnsUtils snsUtils = new SnsUtils();
		try {
			snsUtils.createSNSTopic(client, request.getTopicName());
			client.close();
		} catch (Exception e) {
			return e.getMessage();
		}
		return "SUCCESS";

	}

	@PostMapping("/sns/addsubscriber")
	public String addSubscriber(@RequestBody SNSSubscriberParam request) {
		log.info("addSubscriber:"+request.getArn());

		SnsClient client = SnsClient.builder().region(region).build();

		SnsUtils snsUtils = new SnsUtils();
		try {
			snsUtils.subscribe(client, request.getArn(),request.getProtocol(),request.getEndpoint());
			client.close();
		} catch (Exception e) {
			return e.getMessage();
		}
		return "SUCCESS";

	}
	@PostMapping("/sns/deletesubscriber")
	public String deleteSNSSubscriber(@RequestBody SNSSubscriberParam request) {
		log.info("deleteSubscriber:"+request.getArn());

		SnsClient client = SnsClient.builder().region(region).build();

		SnsUtils snsUtils = new SnsUtils();
		try {
			snsUtils.unSub(client, request.getArn());
			client.close();
		} catch (Exception e) {
			return e.getMessage();
		}
		return "SUCCESS";

	}
	
	@PostMapping("/sns/publish")
	public String doPublish(@RequestBody SNSMessageParam request) {
		log.info("publish message:"+request.getTopicArn()+" "+request.getMessage());

		SnsClient client = SnsClient.builder().region(region).build();

		SnsUtils snsUtils = new SnsUtils();
		try {
			snsUtils.pubTopic(client, request.getMessage(), request.getTopicArn());
			client.close();
		} catch (Exception e) {
			return e.getMessage();
		}
		return "SUCCESS";

	}
	
	@PostMapping("/sqs/addQueue")
	public String addQueue(@RequestBody SQSQueueParam queueParam) {
		log.info("add queue:"+queueParam.getQueueName());

		SqsClient client = SqsClient.builder().region(region).build();

		SqsUtils sqsUtils = new SqsUtils();
		try {
			sqsUtils.addQueue(client, queueParam.getQueueName());
			client.close();
		} catch (Exception e) {
			return e.getMessage();
		}
		return "SUCCESS";

	}
	
	@PostMapping("/sqs/deleteQueue")
	public String deleteQueue(@RequestBody SQSQueueParam queueParam) {
		log.info("delete queue:"+queueParam.getQueueUrl());

		SqsClient client = SqsClient.builder().region(region).build();

		SqsUtils sqsUtils = new SqsUtils();
		try {
			sqsUtils.deleteQueue(client, queueParam.getQueueUrl());
			client.close();
		} catch (Exception e) {
			return e.getMessage();
		}
		return "SUCCESS";

	}
	
	@PostMapping("/sqs/sendMessage")
	public String sendSQSMessage(@RequestBody SQSQueueParam queueParam) {
		log.info("Send queue messsage: "+queueParam.getQueueUrl()+" message: "+queueParam.getMessage());
		SqsClient client = SqsClient.builder().region(region).build();

		SqsUtils sqsUtils = new SqsUtils();
		try {
			sqsUtils.sendSQSMessage(client, queueParam.getQueueUrl(), queueParam.getMessage());
			client.close();
		} catch (Exception e) {
			return e.getMessage();
		}
		return "SUCCESS";
	}
	
	@PostMapping("/sqs/pollMessages")
	public String pollMessages(@RequestBody SQSQueueParam queueParam) {
		log.info("poll queue messsages for: "+queueParam.getQueueUrl());
		SqsClient client = SqsClient.builder().region(region).build();

		String result="";
		SqsUtils sqsUtils = new SqsUtils();
		try {
			result = sqsUtils.pollSQSMessages(client, queueParam.getQueueUrl());
			client.close();
		} catch (Exception e) {
			return e.getMessage();
		}
		return result;
	}
}
