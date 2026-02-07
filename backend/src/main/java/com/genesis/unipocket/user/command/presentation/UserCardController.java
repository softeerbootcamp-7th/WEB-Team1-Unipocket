package com.genesis.unipocket.user.command.presentation;

import com.genesis.unipocket.global.auth.annotation.LoginUser;
import com.genesis.unipocket.user.command.presentation.dto.request.UserCardRequest;
import com.genesis.unipocket.user.command.presentation.dto.response.UserCardResponse;
import com.genesis.unipocket.user.command.service.UserCardService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/cards")
@RequiredArgsConstructor
public class UserCardController {

	private final UserCardService userCardService;

	@PostMapping
	public ResponseEntity<Void> createCard(
			@LoginUser UUID userId, @RequestBody @Valid UserCardRequest request) {
		Long cardId = userCardService.createCard(userId, request);
		return ResponseEntity.created(URI.create("/api/users/cards/" + cardId)).build();
	}

	@GetMapping
	public ResponseEntity<List<UserCardResponse>> getCards(@LoginUser UUID userId) {
		return ResponseEntity.ok(userCardService.getCards(userId));
	}

	@GetMapping("/companies")
	public ResponseEntity<
					List<com.genesis.unipocket.user.command.persistence.entity.enums.CardCompany>>
			getCardCompanies() {
		return ResponseEntity.ok(
				List.of(
						com.genesis.unipocket.user.command.persistence.entity.enums.CardCompany
								.values()));
	}

	@DeleteMapping("/{cardId}")
	public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
		userCardService.deleteCard(cardId);
		return ResponseEntity.noContent().build();
	}
}
