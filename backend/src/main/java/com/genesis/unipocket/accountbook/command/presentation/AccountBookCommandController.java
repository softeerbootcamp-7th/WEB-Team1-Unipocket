package com.genesis.unipocket.accountbook.command.presentation;

import com.genesis.unipocket.accountbook.command.facade.AccountBookOrchestrator;
import com.genesis.unipocket.accountbook.command.presentation.dto.request.AccountBookCreateRequest;
import com.genesis.unipocket.accountbook.command.presentation.dto.response.AccountBookCreateResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "가계부 기능")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/account-books")
public class AccountBookCommandController {

	private final AccountBookOrchestrator accountBookOrchestrator;

	@PostMapping
	public ResponseEntity<AccountBookCreateResponse> createAccountBook(
			@Valid @RequestBody AccountBookCreateRequest req) {

		String userId = "UUID-UUID-UUID-UUID";

		var response = accountBookOrchestrator.createAccountBook(userId, req);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
