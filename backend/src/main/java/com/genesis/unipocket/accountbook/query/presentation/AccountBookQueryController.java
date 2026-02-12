package com.genesis.unipocket.accountbook.query.presentation;

import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookDetailResponse;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookExchangeRateResponse;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookSummaryResponse;
import com.genesis.unipocket.accountbook.query.service.AccountBookQueryService;
import com.genesis.unipocket.auth.common.annotation.LoginUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "가계부 Query API")
@RestController
@RequestMapping("/account-books")
@RequiredArgsConstructor
public class AccountBookQueryController {

	private final AccountBookQueryService accountBookQueryService;

	@GetMapping
	public ResponseEntity<List<AccountBookSummaryResponse>> getAccountBooks(
			@LoginUser UUID userId) {
		return ResponseEntity.ok(accountBookQueryService.getAccountBooks(userId.toString()));
	}

	@GetMapping("/{accountBookId}")
	public ResponseEntity<AccountBookDetailResponse> getAccountBook(
			@LoginUser UUID userId, @PathVariable Long accountBookId) {
		return ResponseEntity.ok(
				accountBookQueryService.getAccountBookDetail(userId.toString(), accountBookId));
	}

	@GetMapping("/{accountBookId}/exchange-rate")
	public ResponseEntity<AccountBookExchangeRateResponse> getAccountBookExchangeRate(
			@LoginUser UUID userId, @PathVariable Long accountBookId) {
		return ResponseEntity.ok(
				accountBookQueryService.getAccountBookExchangeRate(
						userId.toString(), accountBookId));
	}
}
