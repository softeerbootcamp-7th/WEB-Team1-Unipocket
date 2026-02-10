package com.genesis.unipocket.user.query.presentation;

import com.genesis.unipocket.auth.annotation.LoginUser;
import com.genesis.unipocket.user.command.persistence.entity.enums.CardCompany;
import com.genesis.unipocket.user.query.persistence.response.UserCardQueryResponse;
import com.genesis.unipocket.user.query.persistence.response.UserQueryResponse;
import com.genesis.unipocket.user.query.service.UserQueryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "유저 Query API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserQueryController {

	private final UserQueryService userQueryService;

	@GetMapping("/me")
	public ResponseEntity<UserQueryResponse> getMyInfo(@LoginUser UUID userId) {
		return ResponseEntity.ok(userQueryService.getUserInfo(userId));
	}

	@GetMapping("/cards")
	public ResponseEntity<List<UserCardQueryResponse>> getCards(@LoginUser UUID userId) {
		return ResponseEntity.ok(userQueryService.getCards(userId));
	}

	@GetMapping("/cards/companies")
	public ResponseEntity<List<CardCompany>> getCardCompanies() {
		return ResponseEntity.ok(userQueryService.getCardCompanies());
	}
}
