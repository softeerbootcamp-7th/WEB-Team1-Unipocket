package com.genesis.unipocket.global.controller;

import com.genesis.unipocket.global.infrastructure.storage.s3.S3Service;
import com.genesis.unipocket.media.command.application.result.PresignedUrlResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "로컬 이미지 업로드 테스트")
@RestController
@Profile("local")
@RequestMapping("/images/test")
@RequiredArgsConstructor
class LocalImageTestController {

	private final S3Service s3Service;

	@Operation(
			summary = "로컬 Presigned URL 발급",
			description = "로컬 프로필에서 테스트용 presigned URL과 object key를 발급합니다.")
	@PostMapping("/presigned-url")
	public ResponseEntity<LocalPresignedIssueResponse> issuePresignedUrl(
			@RequestParam(defaultValue = "local-test") String prefix,
			@RequestParam String fileName) {
		if (fileName == null || fileName.isBlank()) {
			return ResponseEntity.badRequest()
					.body(new LocalPresignedIssueResponse(null, null, "fileName is required"));
		}

		PresignedUrlResult response = s3Service.getPresignedUrl(prefix, fileName);
		return ResponseEntity.ok(
				new LocalPresignedIssueResponse(
						response.presignedUrl(), response.imageKey(), null));
	}

	@Operation(
			summary = "로컬 객체 존재 확인",
			description = "전달한 object key가 스토리지에 존재하는지 검사합니다.")
	@GetMapping("/exists")
	public ResponseEntity<LocalObjectExistsResponse> checkObjectExists(@RequestParam String key) {
		if (key == null || key.isBlank()) {
			return ResponseEntity.badRequest()
					.body(new LocalObjectExistsResponse(null, false, "key is required"));
		}

		boolean exists = s3Service.validateExists(key);
		return ResponseEntity.ok(new LocalObjectExistsResponse(key, exists, null));
	}

	record LocalPresignedIssueResponse(String presignedUrl, String key, String message) {}

	record LocalObjectExistsResponse(String key, boolean exists, String message) {}
}
