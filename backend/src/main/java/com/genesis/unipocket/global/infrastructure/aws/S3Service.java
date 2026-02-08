package com.genesis.unipocket.global.infrastructure.aws;

import java.net.URL;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class S3Service {

	private final S3Presigner s3Presigner;

	@Value("${spring.cloud.aws.s3.bucket}")
	private String bucketName;

	/**
	 * Presigned URL 생성 (업로드용)
	 *
	 * @param prefix           파일 경로 prefix (예: travel/thumnail)
	 * @param originalFileName 원본 파일명 (확장자 추출용)
	 * @return PresignedUrlResponse
	 */
	public PresignedUrlResponse getPresignedUrl(String prefix, String originalFileName) {
		String extension = "";
		if (originalFileName != null && originalFileName.contains(".")) {
			extension = originalFileName.substring(originalFileName.lastIndexOf("."));
		}

		String fileName = prefix + "/" + UUID.randomUUID() + extension;

		PutObjectRequest objectRequest =
				PutObjectRequest.builder()
						.bucket(bucketName)
						.key(fileName)
						.contentType(
								"image/jpeg") // or generic binary, but image is safer for browser
						// display
						.build();

		PutObjectPresignRequest presignRequest =
				PutObjectPresignRequest.builder()
						.signatureDuration(Duration.ofMinutes(5))
						.putObjectRequest(objectRequest)
						.build();

		PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
		URL url = presignedRequest.url();

		return new PresignedUrlResponse(url.toString(), fileName);
	}

	/**
	 * Presigned URL 생성 (다운로드/읽기용)
	 *
	 * @param s3Key      S3 객체 키
	 * @param expiration 만료 시간
	 * @return Presigned GET URL
	 */
	public String getPresignedGetUrl(String s3Key, Duration expiration) {
		GetObjectRequest getObjectRequest =
				GetObjectRequest.builder().bucket(bucketName).key(s3Key).build();

		GetObjectPresignRequest presignRequest =
				GetObjectPresignRequest.builder()
						.signatureDuration(expiration)
						.getObjectRequest(getObjectRequest)
						.build();

		PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

		return presignedRequest.url().toString();
	}

	public record PresignedUrlResponse(String presignedUrl, String imageKey) {}
}
