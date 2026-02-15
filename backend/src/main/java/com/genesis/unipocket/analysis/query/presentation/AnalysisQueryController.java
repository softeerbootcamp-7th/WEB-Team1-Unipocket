package com.genesis.unipocket.analysis.query.presentation;

import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import com.genesis.unipocket.analysis.query.persistence.response.AccountBookAnalysisRes;
import com.genesis.unipocket.analysis.query.service.AnalysisQueryService;
import com.genesis.unipocket.auth.common.annotation.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "가계부 분석 API")
@RestController
@RequiredArgsConstructor
public class AnalysisQueryController {

	private final AnalysisQueryService analysisQueryService;

	@Operation(summary = "가계부 분석 조회", description = "연/월 및 통화 기준으로 가계부 분석 데이터를 조회합니다.")
	@GetMapping("/account-books/{accountBookId}/analysis")
	public ResponseEntity<AccountBookAnalysisRes> getAnalysis(
			@LoginUser UUID userId,
			@PathVariable Long accountBookId,
			@RequestParam CurrencyType currencyType,
			@RequestParam int year,
			@RequestParam int month) {
		return ResponseEntity.ok(
				analysisQueryService.getAnalysis(userId, accountBookId, currencyType, year, month));
	}
}
