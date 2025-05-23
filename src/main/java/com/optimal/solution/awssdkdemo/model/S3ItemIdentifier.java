package com.optimal.solution.awssdkdemo.model;

public class S3ItemIdentifier {
	private String bucketName;
	private String itemKey;

	public String getBucketName() {
		return bucketName;
	}
	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}
	public String getItemKey() {
		return itemKey;
	}
	public void setItemKey(String itemKey) {
		this.itemKey = itemKey;
	}

}
