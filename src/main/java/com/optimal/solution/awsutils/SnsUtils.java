package com.optimal.solution.awsutils;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;
import software.amazon.awssdk.services.sns.model.DeleteTopicRequest;
import software.amazon.awssdk.services.sns.model.DeleteTopicResponse;
import software.amazon.awssdk.services.sns.model.GetTopicAttributesRequest;
import software.amazon.awssdk.services.sns.model.GetTopicAttributesResponse;
import software.amazon.awssdk.services.sns.model.ListSubscriptionsByTopicRequest;
import software.amazon.awssdk.services.sns.model.ListSubscriptionsByTopicResponse;
import software.amazon.awssdk.services.sns.model.ListTopicsRequest;
import software.amazon.awssdk.services.sns.model.ListTopicsResponse;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sns.model.SubscribeResponse;
import software.amazon.awssdk.services.sns.model.Subscription;
import software.amazon.awssdk.services.sns.model.Topic;
import software.amazon.awssdk.services.sns.model.UnsubscribeRequest;
import software.amazon.awssdk.services.sns.model.UnsubscribeResponse;

public class SnsUtils {

	Logger log = LoggerFactory.getLogger(SnsUtils.class);

	public List<Topic> listSNSTopics(SnsClient snsClient) throws Exception {
		try {
			ListTopicsRequest request = ListTopicsRequest.builder().build();

			ListTopicsResponse response = snsClient.listTopics(request);
			List<Topic> result = response.topics();
			return result;

		} catch (SnsException e) {
			log.error("Error getting SNS topics: " + e.awsErrorDetails().errorMessage());
			throw new Exception(e.awsErrorDetails().errorMessage());
		}
	}

	public List<Subscription> listSNSSubscriptionsForTopic(SnsClient snsClient, String topicArn) throws Exception {
		try {
			ListSubscriptionsByTopicRequest request = ListSubscriptionsByTopicRequest.builder().topicArn(topicArn)
					.build();

			ListSubscriptionsByTopicResponse response = snsClient.listSubscriptionsByTopic(request);

			return response.subscriptions();
		} catch (SnsException e) {
			log.error("Error getting SNS subscriptions for topic " + topicArn + ": "
					+ e.awsErrorDetails().errorMessage());
			throw new Exception(e.awsErrorDetails().errorMessage());
		}
	}

	public Map<String, String> getTopicAttributes(SnsClient snsClient, String topicArn) throws Exception {
		try {
			GetTopicAttributesRequest request = GetTopicAttributesRequest.builder().topicArn(topicArn).build();

			GetTopicAttributesResponse response = snsClient.getTopicAttributes(request);
			return response.attributes();

		} catch (SnsException e) {
			log.error("Cannot get topic attributes: " + e.awsErrorDetails().errorMessage());
			throw new Exception(e.awsErrorDetails().errorMessage());
		}
	}

	public void deleteSNSTopic(SnsClient snsClient, String topicArn) {
		try {
			DeleteTopicRequest request = DeleteTopicRequest.builder().topicArn(topicArn).build();

			DeleteTopicResponse result = snsClient.deleteTopic(request);
			log.info("delete SNS topic status was " + result.sdkHttpResponse().statusCode());

		} catch (SnsException e) {
			log.error("Error deleting SNS topic: " + e.awsErrorDetails().errorMessage());

		}
	}

	public void createSNSTopic(SnsClient snsClient, String topicName) throws Exception {
		CreateTopicResponse result;
		try {
			CreateTopicRequest request = CreateTopicRequest.builder().name(topicName).build();

			result = snsClient.createTopic(request);
			log.info("create SNS topic status was " + result.sdkHttpResponse().statusCode());
		} catch (SnsException e) {
			log.error("Error create SNS topic: " + e.awsErrorDetails().errorMessage());
			throw new Exception(e.awsErrorDetails().errorMessage());
		}
	}

	public void pubTopic(SnsClient snsClient, String message, String topicArn) throws Exception {
		try {
			PublishRequest request = PublishRequest.builder().message(message).topicArn(topicArn).build();

			PublishResponse result = snsClient.publish(request);
			log.info(result.messageId() + " Message sent. Status is " + result.sdkHttpResponse().statusCode());

		} catch (SnsException e) {
			log.error("Error publish SNS topic: " + e.awsErrorDetails().errorMessage());
			throw new Exception(e.awsErrorDetails().errorMessage());
		}
	}

	public void subscribe(SnsClient snsClient, String topicArn, String protocol, String endpoint) throws Exception{
		try {
			SubscribeRequest subscribeRequest = SubscribeRequest.builder()
			        .topicArn(topicArn)
			        .protocol(protocol)
			        .endpoint(endpoint)
			        .build();
			 SubscribeResponse subscribeResponse = snsClient.subscribe(subscribeRequest);
			 log.info("Subscription added ARN: " + subscribeResponse.subscriptionArn());
		} catch (SnsException e) {
			log.error("Error subscribe SNS topic: " + e.awsErrorDetails().errorMessage());
			throw new Exception(e.awsErrorDetails().errorMessage());
		}
	}

	public void unSub(SnsClient snsClient, String subscriptionArn) throws Exception {
		try {
			UnsubscribeRequest request = UnsubscribeRequest.builder().subscriptionArn(subscriptionArn).build();

			snsClient.unsubscribe(request);
			

		} catch (SnsException e) {
			log.error("Error unsubscribe SNS topic: " + e.awsErrorDetails().errorMessage());
			throw new Exception(e.awsErrorDetails().errorMessage());
		}
	}
}
