package com.genesis.unipocket.accountbook.command.presentation;

import com.genesis.unipocket.accountbook.command.facade.AccountBookCommandFacade;
import com.genesis.unipocket.accountbook.command.presentation.request.AccountBookBudgetUpdateRequest;
import com.genesis.unipocket.accountbook.command.presentation.request.AccountBookCreateRequest;
import com.genesis.unipocket.accountbook.command.presentation.request.AccountBookUpdateRequest;
import com.genesis.unipocket.accountbook.command.presentation.response.AccountBookBudgetUpdateResponse;
import com.genesis.unipocket.accountbook.command.presentation.response.AccountBookResponse;
import com.genesis.unipocket.auth.common.annotation.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

	@Operation(summary = "가계부 생성", description = "사용자의 가계부를 생성하고 생성된 가계부 정보를 반환합니다.")
	@PostMapping
	public ResponseEntity<AccountBookResponse> createAccountBook(
			@LoginUser UUID userId, @RequestBody @Valid AccountBookCreateRequest req) {
		var response = accountBookCommandFacade.createAccountBook(userId, req);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Operation(summary = "가계부 수정", description = "가계부 기본 정보와 예산/통화를 수정하고 최신 가계부 정보를 반환합니다.")
	@PatchMapping("/{accountBookId}")
	public ResponseEntity<AccountBookResponse> updateAccountBook(
			@LoginUser UUID userId,
			@PathVariable Long accountBookId,
			@RequestBody @Valid AccountBookUpdateRequest req) {
		var result = accountBookCommandFacade.updateAccountBook(userId, accountBookId, req);
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "가계부 예산 수정", description = "가계부 예산을 갱신하고 적용 환율 정보를 함께 반환합니다.")
	@PatchMapping("/{accountBookId}/budget")
	public ResponseEntity<AccountBookBudgetUpdateResponse> updateBudget(
			@LoginUser UUID userId,
			@PathVariable Long accountBookId,
			@RequestBody @Valid AccountBookBudgetUpdateRequest req) {
		return ResponseEntity.ok(
				AccountBookBudgetUpdateResponse.from(
						accountBookCommandFacade.updateBudget(userId, accountBookId, req)));
	}

	@Operation(summary = "가계부 삭제", description = "가계부를 삭제하고 콘텐츠 없이 성공 상태를 반환합니다.")
	@DeleteMapping("/{accountBookId}")
	public ResponseEntity<Void> deleteAccountBook(
			@LoginUser UUID userId, @PathVariable Long accountBookId) {
		accountBookCommandFacade.deleteAccountBook(userId, accountBookId);
		return ResponseEntity.noContent().build();
	}
}
