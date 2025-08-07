package com.optimal.solution.awssdkdemo.model;

public class SNSSubscriberParam {
	private String arn;
	private String endpoint;
	private String protocol;

	public String getArn() {
		return arn;
	}

	public void setArn(String arn) {
		this.arn = arn;
	}
	
	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

}
