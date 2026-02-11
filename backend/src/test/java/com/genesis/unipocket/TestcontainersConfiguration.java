package com.genesis.unipocket;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	private static final String DUMMY_BUCKET = "test-bucket";

	@Bean
	@ServiceConnection
	MySQLContainer<?> mysqlContainer() {
		return new MySQLContainer<>(DockerImageName.parse("mysql:8.4.8"));
	}

	@Bean
	@ServiceConnection(name = "redis")
	GenericContainer<?> redisContainer() {
		return new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
				.withExposedPorts(6379);
	}

	@Bean
	LocalStackContainer localStackContainer() {
		return new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.8"))
				.withServices(LocalStackContainer.Service.S3);
	}

	@Bean
	DynamicPropertyRegistrar dynamicPropertyRegistrar(LocalStackContainer localStack) {
		return (registry) -> {
			registry.add("spring.cloud.aws.s3.endpoint", () -> localStack.getEndpointOverride(LocalStackContainer.Service.S3).toString());
			registry.add("spring.cloud.aws.region.static", localStack::getRegion);
			registry.add("spring.cloud.aws.credentials.access-key", localStack::getAccessKey);
			registry.add("spring.cloud.aws.credentials.secret-key", localStack::getSecretKey);
		};
	}

	@Bean
	TestcontainersS3BucketInitializer testcontainersS3BucketInitializer(
			LocalStackContainer localStackContainer) {
		return new TestcontainersS3BucketInitializer(localStackContainer, DUMMY_BUCKET);
	}
}
