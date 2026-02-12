package com.genesis.unipocket.global.controller;

import com.genesis.unipocket.auth.common.annotation.LoginUser;
import com.genesis.unipocket.global.infrastructure.aws.S3Service;
import com.genesis.unipocket.global.infrastructure.aws.S3Service.PresignedUrlResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "이미지 업로드 기능")
@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {

	private final S3Service s3Service;

	/**
	 * 이미지 업로드용 Presigned URL 발급
	 *
	 * <p>인증된 사용자만 이미지 업로드 URL을 발급받을 수 있습니다.
	 *
	 * <p>TODO: 처리율 제한(Rate Limiting) 장치 필요 - 사용자당 시간당/일당 요청 수 제한하여 S3 비용 악용 및 DoS 공격 방지
	 *
	 * @param userId 로그인한 사용자 ID (인증 필수)
	 * @param prefix 저장할 폴더 경로 (예: travel, profile)
	 * @param fileName 원본 파일명 (확장자 유지용)
	 */
	@GetMapping("/presigned-url")
	public ResponseEntity<PresignedUrlResponse> getPresignedUrl(
			@LoginUser UUID userId, @RequestParam String prefix, @RequestParam String fileName) {
		return ResponseEntity.ok(s3Service.getPresignedUrl(prefix, fileName));
	}
}
