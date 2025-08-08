package com.optimal.solution.awsutils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.DeleteQueueRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesResponse;
import software.amazon.awssdk.services.sqs.model.ListQueuesRequest;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

public class SqsUtils {

	Logger log = LoggerFactory.getLogger(SqsUtils.class);

	public List<String> listQueues(SqsClient sqsClient) throws Exception {

		try {
			ListQueuesRequest listQueuesRequest = ListQueuesRequest.builder().build();
			ListQueuesResponse listQueuesResponse = sqsClient.listQueues(listQueuesRequest);
			return listQueuesResponse.queueUrls();
		} catch (SqsException e) {
			log.error("Cannot list queues:" + e.awsErrorDetails().errorMessage());
			throw new Exception(e.awsErrorDetails().errorMessage());
		}
	}

	public void addQueue(SqsClient sqsClient, String queueName) throws Exception {
		try {
			// Create a CreateQueueRequest object
			CreateQueueRequest createQueueRequest = CreateQueueRequest.builder().queueName(queueName).build();

			// Make the API call to create the queue
			CreateQueueResponse createQueueResponse = sqsClient.createQueue(createQueueRequest);

			// The response contains the URL of the newly created queue
			String queueUrl = createQueueResponse.queueUrl();

			log.info("Queue created with URL: " + queueUrl);
		} catch (SqsException e) {
			log.error("Cannot add queue:" + e.awsErrorDetails().errorMessage());
			throw new Exception(e.awsErrorDetails().errorMessage());
		}
	}

	public void deleteQueue(SqsClient sqsClient, String queueUrl) throws Exception {
		log.info("delete queue: " + queueUrl);
		try {
			DeleteQueueRequest deleteQueueRequest = DeleteQueueRequest.builder().queueUrl(queueUrl).build();
			// Delete the queue
			sqsClient.deleteQueue(deleteQueueRequest);
		} catch (SqsException e) {
			log.error("Cannot delete queue:" + e.awsErrorDetails().errorMessage());
			throw new Exception(e.awsErrorDetails().errorMessage());
		}
	}

	public Map<String, String> getQueueAttributes(SqsClient sqsClient, String queueUrl) throws Exception {
		Map<String, String> result = new HashMap<String, String>();
		try {
			GetQueueAttributesRequest attributesRequest = GetQueueAttributesRequest.builder().queueUrl(queueUrl)
					.attributeNames(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES,
							QueueAttributeName.VISIBILITY_TIMEOUT, QueueAttributeName.QUEUE_ARN)
					.build();

			GetQueueAttributesResponse attributesResponse = sqsClient.getQueueAttributes(attributesRequest);
			Map<QueueAttributeName, String> attributes = attributesResponse.attributes();
			//result.put("Approximate Messages", attributes.get(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES));
			result.put("Visibility Timeout", attributes.get(QueueAttributeName.VISIBILITY_TIMEOUT) + " seconds");
			result.put("Queue ARN", attributes.get(QueueAttributeName.QUEUE_ARN));
			return result;
		} catch (Exception e) {
			log.error("Cannot get SQS Attributes " + e.getMessage());
			throw new Exception(e.getMessage());
		}
	}

	public void sendSQSMessage(SqsClient sqsClient, String queueUrl, String message) throws Exception {
		SendMessageRequest sendRequest = SendMessageRequest.builder().queueUrl(queueUrl).messageBody(message)
				.delaySeconds(3) // Optional: Delays the message for some time
				.build();

		try {
			SendMessageResponse sendResponse = sqsClient.sendMessage(sendRequest);
			log.info("Message sent. Message ID: " + sendResponse.messageId());
		} catch (Exception e) {
			log.error("Cannot send message: " + e.getMessage());
			throw new Exception(e.getMessage());
		}
	}

	public void deleteMessage(SqsClient sqsClient, String queueUrl, String receiptHandle) {
		try {
			DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder().queueUrl(queueUrl)
					.receiptHandle(receiptHandle).build();

			sqsClient.deleteMessage(deleteMessageRequest);
			// log.info("Message deleted successfully.");
		} catch (Exception e) {
			log.error("Error deleting message: " + e.getMessage());
		}
	}

	public String pollSQSMessages(SqsClient sqsClient, String queueUrl) throws Exception {
		StringBuffer buf = new StringBuffer();
		ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder().queueUrl(queueUrl)
				.maxNumberOfMessages(10) // Receive up to 10 messages at once
				.waitTimeSeconds(3) // Enable long polling for some seconds
				.build();
		try {
			List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();

			if (!messages.isEmpty()) {
				log.info("Received " + messages.size() + " messages.");
				for (Message message : messages) {
					log.info("  Message Body: " + message.body());

					// Process the message (e.g., save to a database, call another service)
					buf.append(message.body() + "\n");

					// Once processed, delete the message from the queue
					deleteMessage(sqsClient, queueUrl, message.receiptHandle());
				}
			} else {
				log.info("No messages received during this poll.");
			}
		} catch (Exception e) {
			log.error("Cannot send message: " + e.getMessage());
			throw new Exception(e.getMessage());
		}
		return buf.toString();
	}
}
