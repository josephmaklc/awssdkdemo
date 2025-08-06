package com.optimal.solution.awsutils;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.DeleteFunctionRequest;
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration;
import software.amazon.awssdk.services.lambda.model.GetFunctionRequest;
import software.amazon.awssdk.services.lambda.model.GetFunctionResponse;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.lambda.model.LambdaException;
import software.amazon.awssdk.services.lambda.model.ListFunctionsResponse;

public class LambdaUtils {

	Logger log = LoggerFactory.getLogger(LambdaUtils.class);
	
	public List<String> listFunctions(LambdaClient awsLambda) throws Exception{
		List<String> result = new ArrayList<String>();
		try {
			ListFunctionsResponse functionResult = awsLambda.listFunctions();
			List<FunctionConfiguration> list = functionResult.functions();
			for (FunctionConfiguration config : list) {
				result.add( config.functionName());
			}

		} catch (LambdaException e) {
			log.error("Error listing lambda functions: "+e.getMessage());
			throw new Exception(e.getMessage());
		}
		return result;
	}

	/**
	 * Invokes a specific AWS Lambda function.
	 *
	 * @param awsLambda    an instance of {@link LambdaClient} to interact with the
	 *                     AWS Lambda service
	 * @param functionName the name of the AWS Lambda function to be invoked
	 */
	public String invokeFunction(LambdaClient awsLambda, String functionName, String json) throws Exception {
		log.info("Invoking function "+functionName+" payload: "+json);
		InvokeResponse res;
		try {
			SdkBytes payload = SdkBytes.fromUtf8String(json);

			InvokeRequest request = InvokeRequest.builder().functionName(functionName).payload(payload).build();

			res = awsLambda.invoke(request);
			String value = res.payload().asUtf8String();
			return value;

		} catch (LambdaException e) {
			log.error("Error invoking lambda function: "+e.getMessage());
			throw new Exception(e.getMessage());
		}
	}

	public void deleteFunction(LambdaClient awsLambda, String functionName) throws Exception {
		try {
			DeleteFunctionRequest request = DeleteFunctionRequest.builder().functionName(functionName).build();

			awsLambda.deleteFunction(request);
			

		} catch (LambdaException e) {
			log.error("Error deleting lambda function: "+e.getMessage());
			throw new Exception(e.getMessage());
		}
	}
	
    public FunctionConfiguration getFunctionDetails(LambdaClient awsLambda, String functionName) throws Exception {
        try {
            GetFunctionRequest functionRequest = GetFunctionRequest.builder()
                .functionName(functionName)
                .build();

            GetFunctionResponse response = awsLambda.getFunction(functionRequest);
            /*
             *  FunctionConfiguration config = getFunctionResponse.configuration();

            System.out.println("Function ARN: " + config.functionArn());
            System.out.println("Function Name: " + config.functionName());
            System.out.println("Runtime: " + config.runtimeAsString());
            System.out.println("Handler: " + config.handler());
            System.out.println("Memory Size: " + config.memorySize() + " MB");
            System.out.println("Timeout: " + config.timeout() + " seconds");
            System.out.println("Description: " + config.description());
            System.out.println("Last Modified: " + config.lastModified());
            System.out.println("IAM Role ARN: " + config.role());

            if (config.hasEnvironment()) {
                System.out.println("\nEnvironment Variables:");
                config.environment().variables().forEach((key, value) ->
                    System.out.println("  " + key + " = " + value)
                );
            }
             */
            FunctionConfiguration config = response.configuration();
            return config;

        } catch (LambdaException e) {
        	log.error("Error deleting lambda function details: "+e.getMessage());
			throw new Exception(e.getMessage());
        }
    }
}
