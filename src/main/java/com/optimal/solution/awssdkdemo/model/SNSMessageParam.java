package com.optimal.solution.awssdkdemo.model;

public class SNSMessageParam {
	private String topicArn;
	private String message;

	public String getTopicArn() {
		return topicArn;
	}
	public void setTopicArn(String topicArn) {
		this.topicArn = topicArn;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
