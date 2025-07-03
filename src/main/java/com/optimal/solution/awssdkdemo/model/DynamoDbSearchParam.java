package com.optimal.solution.awssdkdemo.model;

public class DynamoDbSearchParam {

	private String tableName;
	private String primaryKeyName;
	private String primaryKeyType;
	private String primaryKeyValue;
	private String sortKeyName;
	private String sortKeyType;
	private String sortKeyValue;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getPrimaryKeyType() {
		return primaryKeyType;
	}

	public void setPrimaryKeyType(String primaryKeyType) {
		this.primaryKeyType = primaryKeyType;
	}

	public String getPrimaryKeyValue() {
		return primaryKeyValue;
	}

	public void setPrimaryKeyValue(String primaryKeyValue) {
		this.primaryKeyValue = primaryKeyValue;
	}

	public String getSortKeyType() {
		return sortKeyType;
	}

	public void setSortKeyType(String sortKeyType) {
		this.sortKeyType = sortKeyType;
	}

	public String getSortKeyValue() {
		return sortKeyValue;
	}

	public void setSortKeyValue(String sortKeyValue) {
		this.sortKeyValue = sortKeyValue;
	}
	
	public String getPrimaryKeyName() {
		return primaryKeyName;
	}

	public void setPrimaryKeyName(String primaryKeyName) {
		this.primaryKeyName = primaryKeyName;
	}

	public String getSortKeyName() {
		return sortKeyName;
	}

	public void setSortKeyName(String sortKeyName) {
		this.sortKeyName = sortKeyName;
	}

}
