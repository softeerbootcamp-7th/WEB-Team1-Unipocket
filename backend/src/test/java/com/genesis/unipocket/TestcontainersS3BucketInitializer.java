package com.genesis.unipocket;

import org.testcontainers.containers.localstack.LocalStackContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

public class TestcontainersS3BucketInitializer {

	private final LocalStackContainer localStackContainer;
	private final String bucketName;

	public TestcontainersS3BucketInitializer(
			LocalStackContainer localStackContainer, String bucketName) {
		this.localStackContainer = localStackContainer;
		this.bucketName = bucketName;
		initializeBucket();
	}

	private void initializeBucket() {
		try (S3Client s3Client =
				S3Client.builder()
						.endpointOverride(
								localStackContainer.getEndpointOverride(
										LocalStackContainer.Service.S3))
						.region(Region.of(localStackContainer.getRegion()))
						.credentialsProvider(
								StaticCredentialsProvider.create(
										AwsBasicCredentials.create(
												localStackContainer.getAccessKey(),
												localStackContainer.getSecretKey())))
						.forcePathStyle(true)
						.build()) {
			s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
		} catch (BucketAlreadyOwnedByYouException ignored) {
			// LocalStack S3 버킷이 이미 존재하는 경우는 정상 시나리오다.
		}
	}
}
