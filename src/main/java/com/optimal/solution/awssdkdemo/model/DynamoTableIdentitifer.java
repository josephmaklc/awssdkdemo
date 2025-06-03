package com.optimal.solution.awssdkdemo.model;

// This is for the Request json parameter for ajax call

public class DynamoTableIdentitifer {
	private String tableName;
	private String primaryKey;
	private String primaryKeyType;
	private String sortKey;
	private String sortKeyType;
	
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public String getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}

	public String getPrimaryKeyType() {
		return primaryKeyType;
	}

	public void setPrimaryKeyType(String primaryKeyType) {
		this.primaryKeyType = primaryKeyType;
	}

	public String getSortKey() {
		return sortKey;
	}

	public void setSortKey(String sortKey) {
		this.sortKey = sortKey;
	}

	public String getSortKeyType() {
		return sortKeyType;
	}

	public void setSortKeyType(String sortKeyType) {
		this.sortKeyType = sortKeyType;
	}

}
