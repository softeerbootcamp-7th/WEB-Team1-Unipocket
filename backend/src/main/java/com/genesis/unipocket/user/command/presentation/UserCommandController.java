package com.genesis.unipocket.user.command.presentation;

import com.genesis.unipocket.auth.common.annotation.LoginUser;
import com.genesis.unipocket.user.command.facade.UserCommandFacade;
import com.genesis.unipocket.user.command.presentation.request.UserCardRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "유저 기능")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserCommandController {

	private final UserCommandFacade userCommandFacade;

	@Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자의 계정과 연관 데이터를 삭제합니다.")
	@DeleteMapping("/me")
	public ResponseEntity<Void> withdraw(@LoginUser UUID userId) {
		userCommandFacade.withdraw(userId);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "카드 등록", description = "사용자 카드 정보를 등록하고 생성된 카드 리소스 위치를 반환합니다.")
	@PostMapping("/cards")
	public ResponseEntity<Void> createCard(
			@LoginUser UUID userId, @RequestBody @Valid UserCardRequest request) {
		Long cardId = userCommandFacade.createCard(request, userId);
		return ResponseEntity.created(URI.create("/users/cards/" + cardId)).build();
	}

	@Operation(summary = "카드 삭제", description = "사용자 카드 식별자로 카드 정보를 삭제합니다.")
	@DeleteMapping("/cards/{cardId}")
	public ResponseEntity<Void> deleteCard(@PathVariable Long cardId, @LoginUser UUID userId) {
		userCommandFacade.deleteCard(cardId, userId);
		return ResponseEntity.noContent().build();
	}
}
