package com.genesis.unipocket.user.controller;

import com.genesis.unipocket.auth.annotation.LoginUser;
import com.genesis.unipocket.user.dto.response.UserResponse;
import com.genesis.unipocket.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "유저 기능")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping("/me")
	public ResponseEntity<UserResponse> getMyInfo(@LoginUser UUID userId) {
		return ResponseEntity.ok(userService.getUserInfo(userId));
	}

	@DeleteMapping("/me")
	public ResponseEntity<Void> withdraw(@LoginUser UUID userId) {
		userService.withdrawUser(userId);
		return ResponseEntity.noContent().build();
	}
}
