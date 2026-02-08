package com.genesis.unipocket.global.controller;

import com.genesis.unipocket.global.infrastructure.aws.S3Service;
import com.genesis.unipocket.global.infrastructure.aws.S3Service.PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

	private final S3Service s3Service;

	/**
	 * 이미지 업로드용 Presigned URL 발급
	 *
	 * @param prefix   저장할 폴더 경로 (예: travel, profile)
	 * @param fileName 원본 파일명 (확장자 유지용)
	 */
	@GetMapping("/presigned-url")
	public ResponseEntity<PresignedUrlResponse> getPresignedUrl(
			@RequestParam String prefix, @RequestParam String fileName) {
		return ResponseEntity.ok(s3Service.getPresignedUrl(prefix, fileName));
	}
}
