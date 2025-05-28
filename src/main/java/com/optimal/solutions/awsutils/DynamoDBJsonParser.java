package com.optimal.solutions.awsutils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;


public class DynamoDBJsonParser {

    public static Map<String, AttributeValue> parseJsonToAttributeValue(String jsonString) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = (JsonNode) objectMapper.readTree(jsonString);

        Map<String, AttributeValue> item = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = ((com.fasterxml.jackson.databind.JsonNode) jsonNode).fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String key = field.getKey();
            JsonNode valueNode = field.getValue();

            item.put(key, parseJsonNodeToAttributeValue(valueNode));
        }

        return item;
    }

    private static AttributeValue parseJsonNodeToAttributeValue(JsonNode jsonNode) throws Exception {
        if (jsonNode.has("S")) {
            return AttributeValue.builder().s(jsonNode.get("S").asText()).build();
        } else if (jsonNode.has("N")) {
            return AttributeValue.builder().n(jsonNode.get("N").asText()).build();
        } else if (jsonNode.has("BOOL")) {
            return AttributeValue.builder().bool(jsonNode.get("BOOL").asBoolean()).build();
        } else if (jsonNode.has("NULL")) {
            return AttributeValue.builder().nul(jsonNode.get("NULL").asBoolean()).build();
        } else if (jsonNode.has("SS")) {
            return AttributeValue.builder().ss(jsonNode.get("SS").findValuesAsText("SS")).build();
        } else if (jsonNode.has("NS")) {
            return AttributeValue.builder().ns(jsonNode.get("NS").findValuesAsText("NS")).build();
        } else if (jsonNode.has("M")) {
            Map<String, AttributeValue> nestedMap = parseJsonToAttributeValue(jsonNode.get("M").toString());
            return AttributeValue.builder().m(nestedMap).build();
        } else if (jsonNode.has("L")) {
            List<AttributeValue> list = new ArrayList<>();
            jsonNode.get("L").forEach(element -> {
				try {
					list.add(parseJsonNodeToAttributeValue(element));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
            return AttributeValue.builder().l(list).build();
        }
        // Additional types (e.g. BS, Binary) can be handled similarly.

        throw new RuntimeException("Unknown data type");
    }

    public static void main(String[] args) {
        String dynamoDbJson = "{\"Name\": {\"S\": \"Alice\"}, \"Age\": {\"N\": \"30\"}}";

        String jsonData = """
	    		{
	    "userId": {
	        "S": "123"
	    },
	    "createdAt": {
	        "S": "2022-08-20"
	    },
	    "firstname": {
	        "S": "Sandro"
	    },
	    "lastname": {
	        "S": "Volpicella"
	    },
	    "subscription": {
	        "M": {
	            "customer_id": {
	                "S": "cus_123"
	            },
	            "plan_id": {
	                "S": "plan_123"
	            }
	        }
	    }
	}
	""";
String jsonData2="""
		{
    "name": {
        "S": "Sagat"
    },
    "createdAt": {
        "S": "2022-08-20"
    },
    "firstname": {
        "S": "Tiger"
    },
    "lastname": {
        "S": "Sagatto"
    },
    "orderIds": {
        "L": [
            {
                "S": "123"
            },
            {
                "N": "456"
            }
        ]
    }
}
		""";
        try {
            Map<String, AttributeValue> item = parseJsonToAttributeValue(jsonData2);
            System.out.println(item);
            
            item.forEach((key, value) -> {
                System.out.println("Key: " + key + ", Value: " + value);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}