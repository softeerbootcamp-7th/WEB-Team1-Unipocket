package com.genesis.unipocket.global.infrastructure.storage.s3;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.infrastructure.MediaContentType;
import com.genesis.unipocket.media.command.application.result.PresignedUrlResult;
import io.awspring.cloud.s3.S3Template;
import java.time.Duration;
import java.util.List;
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

	@Value("${spring.cloud.aws.s3.bucket}")
	private String bucketName;

	@Value("${app.media.presigned-put-expiration-seconds}")
	private long putExpirationSeconds;

	@Value("${app.media.presigned-get-expiration-seconds}")
	private long getExpirationSeconds;

	private final S3Template s3Template; // 일반적인 업로드/다운로드용
	private final S3Presigner s3Presigner; // Presigned URL 생성용

	public PresignedUrlResult getPresignedUrl(String prefix, String originalFileName) {
		String extension = extractExtension(originalFileName);
		MediaContentType mediaContentType =
				MediaContentType.fromExtension(extension)
						.orElseThrow(() -> new BusinessException(ErrorCode.UNSUPPORTED_MEDIA_TYPE));

		String normalizedPrefix = prefix == null || prefix.isBlank() ? "" : prefix.trim();

		String key =
				normalizedPrefix.isEmpty()
						? UUID.randomUUID() + mediaContentType.getExt()
						: normalizedPrefix + "/" + UUID.randomUUID() + mediaContentType.getExt();

		PutObjectRequest objectRequest =
				PutObjectRequest.builder()
						.bucket(bucketName)
						.key(key)
						.contentType(mediaContentType.getMimeType())
						.build();

		PutObjectPresignRequest presignRequest =
				PutObjectPresignRequest.builder()
						.signatureDuration(Duration.ofSeconds(putExpirationSeconds))
						.putObjectRequest(objectRequest)
						.build();

		PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
		return new PresignedUrlResult(presignedRequest.url().toString(), key);
	}

	public boolean validateExists(String filePath) {
		return s3Template.objectExists(bucketName, filePath);
	}

	public String provideViewUrl(String filePath) {
		// 조회용 GetObjectRequest 생성
		GetObjectRequest getObjectRequest =
				GetObjectRequest.builder().bucket(bucketName).key(filePath).build();

		// 3분간 유효한 URL 생성
		PresignedGetObjectRequest presignedRequest =
				s3Presigner.presignGetObject(
						r ->
								r.signatureDuration(Duration.ofSeconds(getExpirationSeconds))
										.getObjectRequest(getObjectRequest));

		return presignedRequest.url().toString();
	}

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

	// S3 내부의 전체 버킷 파일 정보들 조회
	public List<String> listAllObjectKeys() {
		return s3Template.listObjects(bucketName, "").stream()
				.map(resource -> resource.getLocation().getObject())
				.toList();
	}

	// 키들에 대해서 오브젝트 삭제 수행
	public void deleteObjects(List<String> keys) {
		keys.forEach(key -> s3Template.deleteObject(bucketName, key));
	}

	public byte[] downloadFile(String key) {
		try {
			return s3Template.download(bucketName, key).getInputStream().readAllBytes();
		} catch (java.io.IOException e) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	private String extractExtension(String fileName) {
		if (fileName == null || fileName.isBlank() || !fileName.contains(".")) {
			throw new IllegalArgumentException("fileName with extension is required");
		}
		return fileName.substring(fileName.lastIndexOf("."));
	}
}
