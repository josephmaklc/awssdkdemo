package com.optimal.solutions.awsutils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeAction;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue.Builder;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.CreateTableResponse;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

public class DynamoUtils {
//https://aws.amazon.com/blogs/database/choosing-the-right-dynamodb-partition-key/

	Logger log = LoggerFactory.getLogger(DynamoUtils.class);

	public List<String> listAllTables(DynamoDbClient ddb) {
		boolean moreTables = true;
		String lastTableName = null;
		List<String> result = new ArrayList<String>();
		while (moreTables) {
			try {
				ListTablesResponse response = null;
				if (lastTableName == null) {
					ListTablesRequest request = ListTablesRequest.builder().build();
					response = ddb.listTables(request);
				} else {
					ListTablesRequest request = ListTablesRequest.builder().exclusiveStartTableName(lastTableName)
							.build();
					response = ddb.listTables(request);
				}

				List<String> tableNames = response.tableNames();
				if (tableNames.size() > 0) {
					result.addAll(tableNames);
					// for (String tableName : tableNames) {
					// System.out.println(tableName);
					// describeDymamoDBTable(ddb, tableName);
					// }
				} else {
					log.info("No dynamoDB tables found!");
					return result;
				}

				lastTableName = response.lastEvaluatedTableName();
				if (lastTableName == null) {
					moreTables = false;
				}

			} catch (DynamoDbException e) {
				log.error("Error listing dynamo tables", e);
				throw e;
			}
		}
		return result;
	}

	public TableDescription describeDymamoDBTable(DynamoDbClient ddb, String tableName) throws Exception {
		DescribeTableRequest request = DescribeTableRequest.builder().tableName(tableName).build();

		TableDescription tableInfo = ddb.describeTable(request).table();
		if (tableInfo != null) {
			return tableInfo;
		}
		return null;
	}

	private String describeAttributeValue(AttributeValue value) {
		return (value.s() == null ? "" : "S=[" + value.s() + "]") + (value.n() == null ? "" : "N=[" + value.n() + "]") // S
				+ (value.l() == null || value.l().isEmpty() ? "" : "L=[" + value.l() + "]") // L is list
				+ (value.ss() == null || value.ss().isEmpty() ? "" : "SS=[" + value.ss() + "]") // SS is string set
				+ (value.ns() == null || value.ns().isEmpty() ? "" : "NS=[" + value.ns() + "]") // NS is number set
				+ (value.m() == null || value.m().isEmpty() ? "" : "M=[" + value.m() + "]"); // M is map
	}

	public void getDynamoDBItem(DynamoDbClient ddb, String tableName, String key, String keyVal) {
		System.out.format("Searching for %s=%s in table %s:\n", key, keyVal, tableName);
		HashMap<String, AttributeValue> keyToGet = new HashMap<>();
		keyToGet.put(key, AttributeValue.builder().s(keyVal).build());

		GetItemRequest request = GetItemRequest.builder().key(keyToGet).tableName(tableName).build();

		try {
			// If there is no matching item, GetItem does not return any data.
			Map<String, AttributeValue> returnedItem = ddb.getItem(request).item();
			if (returnedItem.isEmpty())
				System.out.format("No item found with the key '%s' of value '%s'!\n", key, keyVal);
			else {
				Set<String> keys = returnedItem.keySet();
				for (String aKey : keys) {
					AttributeValue value = returnedItem.get(aKey);
					System.out.format("%s: %s\n", aKey, describeAttributeValue(value));
				}
			}

		} catch (DynamoDbException e) {
			log.error(e.getMessage());
		}
	}

	public int queryTable(DynamoDbClient ddb, String tableName, String partitionKeyName, String partitionKeyVal,
			String partitionAlias) {
		System.out.format("QueryTable for %s=%s in table %s:\n", partitionKeyName, partitionKeyVal, tableName);
		// Set up an alias for the partition key name in case it's a reserved word.
		HashMap<String, String> attrNameAlias = new HashMap<String, String>();
		attrNameAlias.put(partitionAlias, partitionKeyName);

		// Set up mapping of the partition name with the value.
		HashMap<String, AttributeValue> attrValues = new HashMap<>();
		attrValues.put(":" + partitionKeyName, AttributeValue.builder().s(partitionKeyVal).build());

		QueryRequest queryReq = QueryRequest.builder().tableName(tableName)
				.keyConditionExpression(partitionAlias + " = :" + partitionKeyName)
				.expressionAttributeNames(attrNameAlias).expressionAttributeValues(attrValues).build();

		try {
			QueryResponse response = ddb.query(queryReq);
			for (Map<String, AttributeValue> item : response.items()) {
				for (Map.Entry<String, AttributeValue> kvp : item.entrySet()) {

					String attributeName = kvp.getKey();
					AttributeValue value = kvp.getValue();
					System.out.println(attributeName + ":" + describeAttributeValue(value));
				}
			}

			return response.count();

		} catch (DynamoDbException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		return -1;
	}

	public int getItemS(DynamoDbClient ddb, String tableName, String key, String keyVal, String partitionAlias) {

		HashMap<String, AttributeValue> keyToGet = new HashMap<>();
		keyToGet.put(key, AttributeValue.builder().s(keyVal).build());

		GetItemRequest request = GetItemRequest.builder().key(keyToGet).tableName(tableName).build();

		try {
			// If there is no matching item, GetItem does not return any data.
			Map<String, AttributeValue> returnedItem = ddb.getItem(request).item();
			if (returnedItem.isEmpty())
				System.out.format("No item found with the key %s!\n", key);
			else {
				Set<String> keys = returnedItem.keySet();
				System.out.println("Amazon DynamoDB table attributes: \n");
				for (String key1 : keys) {
					System.out.format("%s: %s\n", key1, returnedItem.get(key1).toString());
				}
			}

		} catch (DynamoDbException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		return 0;
	}

	public int getItemComposite(DynamoDbClient ddb, String tableName, String keyName, String keyVal, String sortKeyName,
			String sortKeyValue) {

		// Create the key map
		Map<String, AttributeValue> key = new HashMap<>();
		key.put(keyName, AttributeValue.builder().n(keyVal).build());
		key.put(sortKeyName, AttributeValue.builder().s(sortKeyValue).build());

		// Build the GetItem request
		GetItemRequest request = GetItemRequest.builder().tableName(tableName).key(key).build();

		try {
			// If there is no matching item, GetItem does not return any data.
			Map<String, AttributeValue> returnedItem = ddb.getItem(request).item();
			if (returnedItem.isEmpty())
				System.out.format("No item found with the key %s!\n", key);
			else {
				Set<String> keys = returnedItem.keySet();
				System.out.println("Amazon DynamoDB table attributes: \n");
				for (String key1 : keys) {
					System.out.format("%s: %s\n", key1, returnedItem.get(key1).toString());
				}
			}

		} catch (DynamoDbException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		return 0;

	}

	public List<Map<String, AttributeValue>> queryItemCompositeKey(DynamoDbClient ddb, String tableName,
			String partitionKeyName, String partitionKeyType, String partitionKeyValue, String sortKeyName,
			String sortKeyType, String sortKeyValue) {

		// See
		// https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_Query.html
		Map<String, AttributeValue> expressionValues = new HashMap<>();

		AttributeValue attributeValuePK = null;
		if ("N".equals(partitionKeyType))
			attributeValuePK = AttributeValue.builder().n(partitionKeyValue).build();
		if ("S".equals(partitionKeyType))
			attributeValuePK = AttributeValue.builder().s(partitionKeyValue).build();

		expressionValues.put(":pkVal", attributeValuePK);
		Map<String, String> expressionAttributeNames = new HashMap<>();
		expressionAttributeNames.put("#pk", partitionKeyName);
		String keyConditionExpression = "#pk = :pkVal";
		
		if (!Strings.isBlank(sortKeyValue)) {
			AttributeValue attributeValueSK = null;
			if ("N".equals(sortKeyType))
				attributeValueSK = AttributeValue.builder().n(sortKeyValue).build();
			if ("S".equals(sortKeyType))
				attributeValueSK = AttributeValue.builder().s(sortKeyValue).build();
			expressionValues.put(":skVal", attributeValueSK);
			expressionAttributeNames.put("#sk", sortKeyName);
			// condition can have more elaborate syntax, see the reference link above.
			keyConditionExpression = "#pk = :pkVal AND #sk = :skVal";
		}

		QueryRequest queryRequest = QueryRequest.builder().tableName(tableName)
//		    .keyConditionExpression("product id=1000 AND product type='Book ID'")
				.keyConditionExpression(keyConditionExpression).expressionAttributeValues(expressionValues)
				.expressionAttributeNames(expressionAttributeNames).build();

		try {
			QueryResponse response = ddb.query(queryRequest);

			if (response.hasItems()) {

				response.items().forEach(item -> {
					log.info("Item found: " + item);
				});

				return response.items();
			} else {
				System.out.println("No items found matching the query.");
			}

		} catch (Exception e) {
			System.err.println("Error querying DynamoDB: " + e.getMessage());
		}
		return null;
	}

	public void putItemInTable(DynamoDbClient ddb, String tableName, HashMap<String, AttributeValue> itemValues) {

		PutItemRequest request = PutItemRequest.builder().tableName(tableName).item(itemValues).build();

		try {
			PutItemResponse response = ddb.putItem(request);
			System.out.println(tableName + " was successfully updated. The request id is "
					+ response.responseMetadata().requestId());

		} catch (ResourceNotFoundException e) {
			System.err.format("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", tableName);
			System.err.println("Be sure that it exists and that you've typed its name correctly!");
			System.exit(1);
		} catch (DynamoDbException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	public void putItemInTable(DynamoDbClient ddb, String tableName, String key, String keyVal, String country,
			int age) {

		HashMap<String, AttributeValue> itemValues = new HashMap<>();
		itemValues.put(key, AttributeValue.builder().s(keyVal).build());

		// individual fields
		itemValues.put("country", AttributeValue.builder().s("country").build());
		itemValues.put("age", AttributeValue.builder().n(age + "").build());

		List<String> greek = List.of("alpha", "beta", "gamma");
		List<String> numbers = List.of("6", "8", "9");
		Map<String, AttributeValue> map = new HashMap<String, AttributeValue>();
		map.put("IL", AttributeValue.builder().s("Illinois").build());
		map.put("MI", AttributeValue.builder().s("Michigan").build());

		List<AttributeValue> attributeValueList = greek.stream().map(item -> AttributeValue.builder().s(item).build())
				.toList();
		itemValues.put("testlist", AttributeValue.builder().l(attributeValueList).build());
		itemValues.put("testlist2", AttributeValue.builder().ss(greek).build());
		itemValues.put("testlist3", AttributeValue.builder().ns(numbers).build());
		itemValues.put("testbool", AttributeValue.builder().bool(true).build());
		itemValues.put("testmap", AttributeValue.builder().m(map).build());

		PutItemRequest request = PutItemRequest.builder().tableName(tableName).item(itemValues).build();

		try {
			PutItemResponse response = ddb.putItem(request);
			System.out.println(tableName + " was successfully updated. The request id is "
					+ response.responseMetadata().requestId());

		} catch (ResourceNotFoundException e) {
			System.err.format("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", tableName);
			System.err.println("Be sure that it exists and that you've typed its name correctly!");
			System.exit(1);
		} catch (DynamoDbException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	public void putJSONItemInTable(DynamoDbClient ddb, String tableName, String jsonData) throws Exception {
		try {
			Map<String, AttributeValue> item = DynamoDBJsonParser.parseJsonToAttributeValue(jsonData);
			System.out.println(item);

			HashMap<String, AttributeValue> itemValues = new HashMap<>();

			item.forEach((key, value) -> {
				System.out.println("Key: " + key + ", Value: " + value);
				itemValues.put(key, value);
			});

			PutItemRequest request = PutItemRequest.builder().tableName(tableName).item(itemValues).build();

			try {
				PutItemResponse response = ddb.putItem(request);
				log.info(tableName + " was successfully updated. The request id is "
						+ response.responseMetadata().requestId());

			} catch (ResourceNotFoundException e) {
				log.error("putJSONItemInTable Error: The Amazon DynamoDB table \"%s\" can't be found.\n", tableName);
				throw e;

			} catch (DynamoDbException e) {
				log.error("DynamoDb Exception in putJSONItemInTable" + e.getMessage());
				throw e;

			}
		} catch (Exception e) {
			log.error("Exception in putJSONItemInTable:" + e.getMessage());
			throw e;
		}
	}

	public void createTable(DynamoDbClient ddb, String tableName, String key, String keyType, String sortKey,
			String sortKeyType) throws Exception {
		if (sortKey.isEmpty()) {
			createTablePrimaryKeyOnly(ddb, tableName, key, keyType);
		} else {
			createTableCompositeKey(ddb, tableName, key, keyType, sortKey, sortKeyType);
		}
	}

	public void createTablePrimaryKeyOnly(DynamoDbClient ddb, String tableName, String key, String keyType)
			throws Exception {

		log.info("creating Dynamno DB table: " + tableName + " key: " + key + " keyType: " + keyType);
		DynamoDbWaiter dbWaiter = ddb.waiter();
		CreateTableRequest request = CreateTableRequest.builder()
				.attributeDefinitions(AttributeDefinition.builder().attributeName(key).attributeType(keyType).build())
				.keySchema(KeySchemaElement.builder().attributeName(key).keyType(KeyType.HASH).build())
				.provisionedThroughput(
						ProvisionedThroughput.builder().readCapacityUnits(10L).writeCapacityUnits(10L).build())
				.tableName(tableName).build();

		try {
			CreateTableResponse response = ddb.createTable(request);
			DescribeTableRequest tableRequest = DescribeTableRequest.builder().tableName(tableName).build();

			// Wait until the Amazon DynamoDB table is created.
			WaiterResponse<DescribeTableResponse> waiterResponse = dbWaiter.waitUntilTableExists(tableRequest);
			waiterResponse.matched().response().ifPresent(System.out::println);
			String newTable = response.tableDescription().tableName();
			System.out.println("Table created: " + newTable);

		} catch (DynamoDbException e) {
			log.error("Error creating table: ", e);
			throw e;
		}
	}

	public void createTableCompositeKey(DynamoDbClient ddb, String tableName, String partitionKey,
			String paritionKeyType, String sortKey, String sortKeyType) {
		System.out.println("creating Dynamo DB table: " + tableName + " key: " + partitionKey + " keyType: "
				+ paritionKeyType + " sortKey:" + sortKey + " sortKeyType:" + sortKeyType);
		;
		CreateTableRequest request = CreateTableRequest.builder()
				.attributeDefinitions(
						AttributeDefinition.builder().attributeName(partitionKey).attributeType(paritionKeyType)
								.build(),
						AttributeDefinition.builder().attributeName(sortKey).attributeType(sortKeyType).build())
				.keySchema(KeySchemaElement.builder().attributeName(partitionKey).keyType(KeyType.HASH).build(),
						KeySchemaElement.builder().attributeName(sortKey).keyType(KeyType.RANGE).build())
				.billingMode(BillingMode.PAY_PER_REQUEST) // DynamoDB automatically scales based on traffic.
				.tableName(tableName).build();

		try {
			CreateTableResponse response = ddb.createTable(request);
			String newTable = response.tableDescription().tableName();
			System.out.println("Table created: " + newTable);

		} catch (DynamoDbException e) {
			System.out.println("Error creating table: " + e.getMessage());
		}
	}

	public void deleteTable(DynamoDbClient ddb, String tableName) throws Exception {
		log.info("Deleting table: " + tableName);
		DeleteTableRequest request = DeleteTableRequest.builder().tableName(tableName).build();

		try {
			ddb.deleteTable(request);
			log.info(tableName + " was successfully deleted!");
		} catch (DynamoDbException e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	private AttributeValue buildAttributeValue(String type, String value) {
		Builder builder = AttributeValue.builder();
		switch (type) {
		case "S":
			builder.s(value);
			break;
		case "N":
			builder.n(value);
			break;
		default:
			System.out.println("Not able to handle this type of attribute yet:" + type);
			builder.s(value);
		}

		return builder.build();
	}

	public void deleteItem(DynamoDbClient ddb, String tableName, String key, String keyType, String keyVal) {
		HashMap<String, AttributeValue> keyToGet = new HashMap<>();
		keyToGet.put(key, buildAttributeValue(keyType, keyVal));

		DeleteItemRequest deleteReq = DeleteItemRequest.builder().tableName(tableName).key(keyToGet).build();

		try {
			ddb.deleteItem(deleteReq);
			System.out.println(
					"table: " + tableName + " key: [" + key + "] val: [" + keyVal + "] was successfully deleted!");

		} catch (DynamoDbException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	public void deleteItemCompositeKey(DynamoDbClient ddb, String tableName, String key, String keyType, String keyVal,
			String sortKey, String sortKeyTYpe, String sortKeyVal) {
		HashMap<String, AttributeValue> keyToGet = new HashMap<>();
		keyToGet.put(key, buildAttributeValue(keyType, keyVal));
		keyToGet.put(sortKey, buildAttributeValue(sortKeyTYpe, sortKeyVal));

		DeleteItemRequest deleteReq = DeleteItemRequest.builder().tableName(tableName).key(keyToGet).build();

		try {
			ddb.deleteItem(deleteReq);
			System.out.println("table: " + tableName + " key: [" + key + "] val: [" + keyVal + "] sortKey: [" + sortKey
					+ "] sortKeyVal: [" + sortKeyVal + "] was successfully deleted!");

		} catch (DynamoDbException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	public void updateTableItem(DynamoDbClient ddb, String tableName, String key, String keyType, String keyVal,
			String fieldName, String fieldType, String updateVal) {

		System.out.println(
				"Updating Table " + tableName + " key: " + key + " value: " + keyVal + " updateVal: " + updateVal);
		HashMap<String, AttributeValue> itemKey = new HashMap<>();
		itemKey.put(key, buildAttributeValue(keyType, keyVal));

		HashMap<String, AttributeValueUpdate> updatedValues = new HashMap<>();
		updatedValues.put(fieldName, AttributeValueUpdate.builder().value(buildAttributeValue(fieldType, updateVal))
				.action(AttributeAction.PUT).build());

		UpdateItemRequest request = UpdateItemRequest.builder().tableName(tableName).key(itemKey)
				.attributeUpdates(updatedValues).build();

		try {
			UpdateItemResponse response = ddb.updateItem(request);
			System.out.println(response);
			System.out.println("Table " + tableName + " was updated");
		} catch (DynamoDbException e) {
			System.err.println(e.getMessage());
		}
	}

	DynamoAttribute splitIntoToDynamoAttribute(AttributeValue attributeValue) {
		String type = "Unknown";
		Object value = null;
		if (attributeValue.s() != null) {
			type = "String";
			value = attributeValue.s();
		} else if (attributeValue.n() != null) {
			type = "Number";
			value = attributeValue.n();
		} else if (attributeValue.bool() != null) {
			type = "Boolean";
			value = attributeValue.bool();
		} else if (attributeValue.m() != null) {
			type = "Map";
			value = attributeValue.m();
		} else if (attributeValue.l() != null) {
			type = "List";
			value = attributeValue.l();
		} else if (attributeValue.b() != null) {
			type = "Byte";
			value = attributeValue.b();
		} else if (attributeValue.ns() != null) {
			type = "Number Set";
			value = attributeValue.ns();
		} else if (attributeValue.ss() != null) {
			type = "String Set";
			value = attributeValue.ss();
		} else if (attributeValue.bs() != null) {
			type = "ByteBuffer Set";
			value = attributeValue.bs();
		} else if (attributeValue.nul() != null && attributeValue.nul()) {
			type = null;
		} else {
			System.out.println("Unknown type for this attribute: " + attributeValue);
		}
		DynamoAttribute result = new DynamoAttribute();
		result.type = type;
		result.value = value;
		return result;

	}

	public static String convertScanResponseToJson(ScanResponse scanResponse) {
		try {
			List<Map<String, AttributeValue>> items = scanResponse.items();
			List<Map<String, Object>> jsonData = new ArrayList<>();
			ObjectMapper objectMapper = new ObjectMapper();

			for (Map<String, AttributeValue> item : items) {
				Map<String, Object> jsonItem = new HashMap<>();
				for (Map.Entry<String, AttributeValue> entry : item.entrySet()) {
					jsonItem.put(entry.getKey(), entry.getValue().s());
				}
				jsonData.add(jsonItem);
				System.out.println("jsonItem: " + objectMapper.writeValueAsString(jsonItem));
			}

			return objectMapper.writeValueAsString(jsonData);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static Map<Object, Object> toSimpleMap(Map<String, AttributeValue> item) {
		// Convert DynamoDB item to a simple map with JSON-friendly values
		System.out.println("inside toSimpleMap: " + item);
		return item.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
			AttributeValue value = entry.getValue();
			if (value.s() != null)
				return value.s();
			if (value.n() != null)
				return Double.parseDouble(value.n());
			if (value.bool() != null)
				return value.bool();
			if (value.m() != null) {
				return toSimpleMap(value.m()); // recursion
			}
			if (value.l() != null)
				return value.l().stream().map(DynamoUtils::toSimpleValue).collect(Collectors.toList());

			// Add other attribute types as needed (e.g., B, SS, NS, BS)
			return null;
		}));
	}

	private static Object toSimpleValue(AttributeValue value) {
		if (value.s() != null)
			return value.s();
		if (value.n() != null)
			return Double.parseDouble(value.n());
		if (value.bool() != null)
			return value.bool();
		if (value.m() != null)
			return toSimpleMap(value.m());
		if (value.l() != null)
			return value.l().stream().map(DynamoUtils::toSimpleValue).collect(Collectors.toList());
		// Add other attribute types as needed (e.g., B, SS, NS, BS)
		return null;
	}

	public List<Map<String, AttributeValue>> scanTable(DynamoDbClient ddb, String tableName) {

		try {
			ScanRequest scanRequest = ScanRequest.builder().tableName(tableName).build();

			ScanResponse response = ddb.scan(scanRequest);

			List<Map<String, AttributeValue>> items = response.items();

			for (Map<String, AttributeValue> item : items) {
				log.info("Item: " + item);

				for (String key : item.keySet()) {
					DynamoAttribute dynamoAttibute = splitIntoToDynamoAttribute(item.get(key));

					log.info(key + "::  type: " + dynamoAttibute.type + " value:" + dynamoAttibute.value);
				}

			}

//			String json = convertScanResponseToJson(response);
//			System.out.println(json);

			ObjectMapper objectMapper = new ObjectMapper();

			items.forEach(item -> {
				try {
					// Convert DynamoDB item to a JSON string
					String jsonString = objectMapper.writeValueAsString(toSimpleMap(item));
					System.out.println("jsonString:" + jsonString);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			return items;

		} catch (DynamoDbException e) {
			log.error("Unable to scan table: " + e.getMessage());
		}
		return null;
	}

}
