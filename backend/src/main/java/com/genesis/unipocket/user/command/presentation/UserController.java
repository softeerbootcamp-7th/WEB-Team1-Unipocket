package com.genesis.unipocket.user.command.presentation;

import com.genesis.unipocket.global.auth.annotation.LoginUser;
import com.genesis.unipocket.user.command.presentation.dto.response.UserResponse;
import com.genesis.unipocket.user.command.service.UserCommandService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserCommandService userCommandService;

	@GetMapping("/me")
	public ResponseEntity<UserResponse> getMyInfo(@LoginUser UUID userId) {
		return ResponseEntity.ok(userCommandService.getUserInfo(userId));
	}

	@DeleteMapping("/me")
	public ResponseEntity<Void> withdraw(@LoginUser UUID userId) {
		userCommandService.withdrawUser(userId);
		return ResponseEntity.noContent().build();
	}
}
