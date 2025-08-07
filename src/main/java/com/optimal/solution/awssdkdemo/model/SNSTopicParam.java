package com.optimal.solution.awssdkdemo.model;

public class SNSTopicParam {
	private String arn;
	private String topicName;

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public String getArn() {
		return arn;
	}

	public void setArn(String arn) {
		this.arn = arn;
	}
}
