package com.genesis.unipocket.accountbook.controller;

import com.genesis.unipocket.accountbook.dto.request.AccountBookCreateRequest;
import com.genesis.unipocket.accountbook.dto.request.AccountBookUpdateRequest;
import com.genesis.unipocket.accountbook.dto.response.AccountBookDetailResponse;
import com.genesis.unipocket.accountbook.dto.response.AccountBookResponse;
import com.genesis.unipocket.accountbook.dto.response.AccountBookSummaryResponse;
import com.genesis.unipocket.accountbook.facade.AccountBookFacade;
import com.genesis.unipocket.auth.annotation.LoginUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "가계부 기능")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/account-books")
public class AccountBookController {

	private final AccountBookFacade accountBookFacade;

	@PostMapping
	public ResponseEntity<AccountBookResponse> createAccountBook(
			@LoginUser UUID userId, @Valid @RequestBody AccountBookCreateRequest req) {

		var response = accountBookFacade.createAccountBook(userId, req);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PutMapping("/{accountBookId}")
	public ResponseEntity<AccountBookResponse> updateAccountBook(
			@LoginUser UUID userId,
			@PathVariable Long accountBookId,
			@Valid @RequestBody AccountBookUpdateRequest req) {

		var response = accountBookFacade.updateAccountBook(userId, accountBookId, req);

		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{accountBookId}")
	public ResponseEntity<Void> deleteAccountBook(
			@LoginUser UUID userId, @PathVariable Long accountBookId) {

		accountBookFacade.deleteAccountBook(userId, accountBookId);

		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{accountBookId}")
	public ResponseEntity<AccountBookDetailResponse> getAccountBook(
			@LoginUser UUID userId, @PathVariable Long accountBookId) {

		var response = accountBookFacade.getAccountBook(userId, accountBookId);

		return ResponseEntity.ok(response);
	}

	@GetMapping
	public ResponseEntity<List<AccountBookSummaryResponse>> getAccountBooks(
			@LoginUser UUID userId) {

		var response = accountBookFacade.getAccountBooks(userId);

		return ResponseEntity.ok(response);
	}
}
