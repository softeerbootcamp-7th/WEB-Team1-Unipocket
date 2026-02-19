package com.genesis.unipocket.dev;

import com.genesis.unipocket.auth.command.application.TokenService;
import com.genesis.unipocket.auth.command.persistence.repository.SocialAuthRepository;
import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import com.genesis.unipocket.user.query.persistence.response.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[DEV] 개발용 API", description = "프론트엔드 개발 편의를 위한 회원가입/토큰 발급 API")
@RestController
@RequestMapping("/dev")
@RequiredArgsConstructor
public class DevController {

	private final SocialAuthRepository socialAuthRepository;
	private final UserCommandRepository userRepository;
	private final TokenService tokenService;

	public record DevSignUpRequest(String email, String name) {}

	public record DevUserResponse(UUID userId, String email, String name, String status) {}

	@Operation(
			summary = "개발용 회원가입 API",
			description = "email, name만으로 간편 회원가입을 수행하고 생성된 userId(UUID)를 반환합니다.")
	@PostMapping("/sign-up")
	public ResponseEntity<UUID> signUp(@RequestBody DevSignUpRequest request) {
		UserEntity user = UserEntity.builder().email(request.email()).name(request.name()).build();
		UserEntity saved = userRepository.save(user);
		return ResponseEntity.ok(saved.getId());
	}

	@Operation(
			summary = "개발용 유저 목록 조회 API",
			description = "최근 가입한 유저 5명의 정보를 반환합니다. 토큰 발급 시 userId 확인용입니다.")
	@GetMapping("/users")
	public ResponseEntity<List<DevUserResponse>> getUsers() {
		List<DevUserResponse> users =
				userRepository.findAll(PageRequest.of(0, 5)).getContent().stream()
						.map(
								u ->
										new DevUserResponse(
												u.getId(),
												u.getEmail(),
												u.getName(),
												u.getStatus().name()))
						.toList();
		return ResponseEntity.ok(users);
	}

	@Operation(
			summary = "개발용 토큰 발급 API",
			description = "userId를 받아 accessToken과 refreshToken을 발급합니다.")
	@PostMapping("/token")
	public ResponseEntity<LoginResponse> issueToken(
			@Parameter(description = "회원가입 시 발급받은 userId (UUID)") @RequestParam UUID userId) {
		var result = tokenService.createTokens(userId);
		LoginResponse response =
				LoginResponse.of(
						result.getAccessToken(),
						result.getRefreshToken(),
						userId,
						result.getExpiresIn().intValue());
		return ResponseEntity.ok(response);
	}
}
