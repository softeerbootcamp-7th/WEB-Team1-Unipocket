package com.genesis.unipocket.global.infrastructure.storage.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.genesis.unipocket.TestcontainersConfiguration;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.media.command.application.result.PresignedUrlResult;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test-it")
@Tag("integration")
class S3ServiceIntegrationTest {

	@Autowired private S3Service s3Service;

	@Test
	@DisplayName("Presigned URL 발급 - 지원 확장자")
	void getPresignedUrl_supportedExtension_success() {
		PresignedUrlResult response = s3Service.getPresignedUrl("it-test/images", "sample.jpg");

		assertThat(response.presignedUrl()).isNotBlank();
		assertThat(response.imageKey()).startsWith("it-test/images/");
		assertThat(response.imageKey()).endsWith(".jpg");
	}

	@Test
	@DisplayName("Presigned URL 발급 - 미지원 확장자")
	void getPresignedUrl_unsupportedExtension_fail() {
		assertThatThrownBy(() -> s3Service.getPresignedUrl("it-test/images", "sample.pdf"))
				.isInstanceOf(BusinessException.class);
	}

	@Test
	@DisplayName("Presigned PUT 업로드/존재확인/삭제 플로우")
	void presignedPut_validateExists_delete_success() throws Exception {
		PresignedUrlResult putInfo = s3Service.getPresignedUrl("it-test/upload", "upload.png");

		putToPresignedUrl(
				putInfo.presignedUrl(),
				"image/png",
				"localstack-test".getBytes(StandardCharsets.UTF_8));

		assertThat(s3Service.validateExists(putInfo.imageKey())).isTrue();

		s3Service.deleteObjects(List.of(putInfo.imageKey()));
		assertThat(s3Service.validateExists(putInfo.imageKey())).isFalse();
	}

	@Test
	@DisplayName("조회용 Presigned GET URL 발급")
	void getPresignedGetUrl_success() throws Exception {
		PresignedUrlResult putInfo = s3Service.getPresignedUrl("it-test/view", "view.jpg");
		putToPresignedUrl(
				putInfo.presignedUrl(), "image/jpeg", "view-test".getBytes(StandardCharsets.UTF_8));

		String presignedGetUrl =
				s3Service.getPresignedGetUrl(putInfo.imageKey(), Duration.ofMinutes(3));
		assertThat(presignedGetUrl).isNotBlank();
		assertThat(presignedGetUrl).contains(putInfo.imageKey());
	}

	private void putToPresignedUrl(String presignedUrl, String contentType, byte[] payload)
			throws Exception {
		HttpURLConnection connection = (HttpURLConnection) new URL(presignedUrl).openConnection();
		connection.setRequestMethod("PUT");
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", contentType);
		connection.getOutputStream().write(payload);

		int status = connection.getResponseCode();
		assertThat(status).isIn(200, 201);
		connection.disconnect();
	}
}
