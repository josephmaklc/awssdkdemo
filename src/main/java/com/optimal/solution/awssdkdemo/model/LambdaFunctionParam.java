package com.optimal.solution.awssdkdemo.model;

public class LambdaFunctionParam {
	private String functionName;
	private String payload;

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}
}
