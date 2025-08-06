package com.optimal.solution.awsutils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

/**
 * This class provides some common actions with S3, such as creating bucket, inserting and deleting files
 */
public class S3Utils {

	Logger log = LoggerFactory.getLogger(S3Utils.class);

	public void createBucket(S3Client s3, String bucketName) throws Exception {
		try {
			log.info("Creating bucket: " + bucketName);

			// Create the bucket request
			CreateBucketRequest createBucketRequest = CreateBucketRequest.builder().bucket(bucketName).build();

			// Create the bucket
			s3.createBucket(createBucketRequest);

			log.info("Bucket created: " + bucketName);

		} catch (S3Exception e) {
			log.error("Error creating bucket: ",e);
			throw new Exception(e.awsErrorDetails().errorMessage());
		}
	}

	public void deleteBucket(S3Client s3, String bucketName) throws Exception {
		try {
			log.info("Deleting bucket: " + bucketName);
			DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder().bucket(bucketName).build();
			s3.deleteBucket(deleteBucketRequest);
			log.info("Bucket deleted successfully.");
		} catch (S3Exception e) {
			log.error("Error creating bucket: " + e.awsErrorDetails().errorMessage());
			throw new Exception(e.awsErrorDetails().errorMessage());
		}

	}

	/**
	 * get content as String
	 */
	public String getS3Object(S3Client s3Client, String bucketName, String keyName) {
		log.info("getS3Object bucketName: "+bucketName+" keyName: " + keyName);
		ResponseBytes<GetObjectResponse> objectBytes = null;
		try {
			GetObjectRequest objectRequest = GetObjectRequest.builder().key(keyName).bucket(bucketName).build();

			objectBytes = s3Client.getObject(objectRequest, ResponseTransformer.toBytes());

		} catch (Exception e) {
			// e.printStackTrace();
			return "Error: " + e.getMessage();
		}
		try {
			// try to give you a string
			return objectBytes.asUtf8String();

		} catch (Exception ex) {

			String tempDir = System.getProperty("java.io.tmpdir");
			String path = tempDir + File.pathSeparator + keyName;
			// Write the data to a local file.
			byte[] data = objectBytes.asByteArray();
			File myFile = new File(path);

			try {
				OutputStream os = new FileOutputStream(myFile);
				os.write(data);
				os.close();
			} catch (Exception e) {
				log.error("Error downloading "+keyName+" from bucket "+bucketName+" from S3",e);
			}
			return path;
		}
	}

	/**
	 * List Items in a given bucket
	 * 
	 */
	public List<String> listItemsInBucket(S3Client s3Client, String bucketName, String prefix) {
		log.info("listItemsInBucket bucketName: "+bucketName+" prefix: " + prefix);
		List<String> result = new ArrayList<String>();

		ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucketName).prefix(prefix).build();
		ListObjectsV2Iterable response = s3Client.listObjectsV2Paginator(request);

		for (ListObjectsV2Response page : response) {
			page.contents().forEach((S3Object object) -> {
				if (!object.key().endsWith("/"))
					result.add(object.key());
			});
		}
		return result;

	}

	/**
	 * Get a list of all the S3 buckets associated with the provided AWS S3 client.
	 *
	 */
	public List<String> listBuckets(S3Client s3Client) throws Exception{
		List<String> result = new ArrayList<String>();
		try {
			ListBucketsResponse response = s3Client.listBuckets();
			List<Bucket> bucketList = response.buckets();
			bucketList.forEach(bucket -> {
				result.add(bucket.name());
			});

		} catch (S3Exception e) {
			log.error("Error listing buckets: "+e.awsErrorDetails().errorMessage(),e);
			throw e;
		}
		return result;
	}

	public void uploadFile(S3Client s3, String bucketName, String objectKey, String objectPath) {
		try {
			log.info("uploading file to S3 bucket: " + bucketName + " objectKey: " + objectKey + " objectPath: " + objectPath);
			Map<String, String> metadata = new HashMap<>();
			// metadata.put("author", "Mary Doe");
			// metadata.put("version", "1.0.0.0");

			PutObjectRequest putOb = PutObjectRequest.builder().bucket(bucketName).key(objectKey).metadata(metadata)
					.build();

			s3.putObject(putOb, RequestBody.fromFile(new File(objectPath)));
			log.info("Successfully placed " + objectKey + " into bucket " + bucketName);

		} catch (S3Exception e) {
			log.error("Error uploading file to S3: "+e.awsErrorDetails().errorMessage(),e);
		}
	}

	public void deleteFileS3(S3Client s3, String bucketName, String objectKey) throws Exception {
		log.info("delete file from S3 bucket:" + bucketName + " objectKey: " + objectKey);
		ArrayList<ObjectIdentifier> keys = new ArrayList<>();
		ObjectIdentifier objectId = ObjectIdentifier.builder().key(objectKey).build();
		keys.add(objectId);
		Delete del = Delete.builder().objects(keys).build();

		try {
			DeleteObjectsRequest multiObjectDeleteRequest = DeleteObjectsRequest.builder().bucket(bucketName)
					.delete(del).build();

			s3.deleteObjects(multiObjectDeleteRequest);
			log.info("deleted " + objectKey + " from bucket " + bucketName);
		} catch (S3Exception e) {
			log.error("Error deleting file from S3: "+e.awsErrorDetails().errorMessage(),e);
			throw e;
		}
	}
}