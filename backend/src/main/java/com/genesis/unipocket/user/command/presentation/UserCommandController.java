package com.genesis.unipocket.user.command.presentation;

import com.genesis.unipocket.auth.common.annotation.LoginUser;
import com.genesis.unipocket.user.command.facade.UserCommandFacade;
import com.genesis.unipocket.user.command.presentation.request.UserCardRequest;
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

@Tag(name = "유저 Command API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserCommandController {

	private final UserCommandFacade userCommandFacade;

	@DeleteMapping("/me")
	public ResponseEntity<Void> withdraw(@LoginUser UUID userId) {
		userCommandFacade.withdraw(userId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/cards")
	public ResponseEntity<Void> createCard(
			@LoginUser UUID userId, @RequestBody @Valid UserCardRequest request) {
		Long cardId = userCommandFacade.createCard(request, userId);
		return ResponseEntity.created(URI.create("/users/cards/" + cardId)).build();
	}

	@DeleteMapping("/cards/{cardId}")
	public ResponseEntity<Void> deleteCard(@PathVariable Long cardId, @LoginUser UUID userId) {
		userCommandFacade.deleteCard(cardId, userId);
		return ResponseEntity.noContent().build();
	}
}
