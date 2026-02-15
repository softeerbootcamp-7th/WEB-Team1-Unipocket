package com.genesis.unipocket.accountbook.query.presentation;

import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookDetailResponse;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookExchangeRateResponse;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookSummaryResponse;
import com.genesis.unipocket.accountbook.query.service.AccountBookQueryService;
import com.genesis.unipocket.auth.common.annotation.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "가계부 기능")
@RestController
@RequestMapping("/account-books")
@RequiredArgsConstructor
public class AccountBookQueryController {

	private final AccountBookQueryService accountBookQueryService;

	@Operation(summary = "가계부 목록 조회", description = "사용자가 접근 가능한 가계부 목록을 조회합니다.")
	@GetMapping
	public ResponseEntity<List<AccountBookSummaryResponse>> getAccountBooks(
			@LoginUser UUID userId) {
		return ResponseEntity.ok(accountBookQueryService.getAccountBooks(userId.toString()));
	}

	@Operation(summary = "가계부 상세 조회", description = "가계부 단건 상세 정보를 조회합니다.")
	@GetMapping("/{accountBookId}")
	public ResponseEntity<AccountBookDetailResponse> getAccountBook(
			@LoginUser UUID userId, @PathVariable Long accountBookId) {
		return ResponseEntity.ok(
				accountBookQueryService.getAccountBookDetail(userId.toString(), accountBookId));
	}

	@Operation(
			summary = "가계부 환율 조회",
			description =
					"Exchange 도메인을 통해 가계부의 기준/현지 통화 환율을 조회합니다. 쿼리파라미터로 시점을 넣지 않으면 현재 시점의 환율을"
							+ " 조회합니다.")
	@GetMapping("/{accountBookId}/exchange-rate")
	public ResponseEntity<AccountBookExchangeRateResponse> getAccountBookExchangeRate(
			@LoginUser UUID userId,
			@PathVariable Long accountBookId,
			@RequestParam(required = false) LocalDateTime occurredAt) {
		return ResponseEntity.ok(
				accountBookQueryService.getAccountBookExchangeRate(
						userId.toString(), accountBookId, occurredAt));
	}
}
