package com.genesis.unipocket.accountbook.command.presentation;

import com.genesis.unipocket.accountbook.command.facade.AccountBookCommandFacade;
import com.genesis.unipocket.accountbook.command.presentation.request.AccountBookBudgetUpdateRequest;
import com.genesis.unipocket.accountbook.command.presentation.request.AccountBookCreateRequest;
import com.genesis.unipocket.accountbook.command.presentation.request.AccountBookUpdateRequest;
import com.genesis.unipocket.accountbook.command.presentation.response.AccountBookBudgetUpdateResponse;
import com.genesis.unipocket.auth.common.annotation.LoginUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "가계부 기능")
@RestController
@RequestMapping("/account-books")
@RequiredArgsConstructor
public class AccountBookCommandController {

	private final AccountBookCommandFacade accountBookCommandFacade;

	@PostMapping
	public ResponseEntity<Void> createAccountBook(
			@LoginUser UUID userId, @RequestBody @Valid AccountBookCreateRequest req) {
		Long id = accountBookCommandFacade.createAccountBook(userId, req);
		return ResponseEntity.created(URI.create("/account-books/" + id)).build();
	}

	@PatchMapping("/{accountBookId}")
	public ResponseEntity<Void> updateAccountBook(
			@LoginUser UUID userId,
			@PathVariable Long accountBookId,
			@RequestBody @Valid AccountBookUpdateRequest req) {
		accountBookCommandFacade.updateAccountBook(userId, accountBookId, req);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{accountBookId}/budget")
	public ResponseEntity<AccountBookBudgetUpdateResponse> updateBudget(
			@LoginUser UUID userId,
			@PathVariable Long accountBookId,
			@RequestBody @Valid AccountBookBudgetUpdateRequest req) {
		return ResponseEntity.ok(
				AccountBookBudgetUpdateResponse.from(
						accountBookCommandFacade.updateBudget(userId, accountBookId, req)));
	}

	@DeleteMapping("/{accountBookId}")
	public ResponseEntity<Void> deleteAccountBook(
			@LoginUser UUID userId, @PathVariable Long accountBookId) {
		accountBookCommandFacade.deleteAccountBook(userId, accountBookId);
		return ResponseEntity.noContent().build();
	}
}
