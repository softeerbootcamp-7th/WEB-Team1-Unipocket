package com.genesis.unipocket.analysis.query.presentation;

import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import com.genesis.unipocket.analysis.query.persistence.response.AnalysisOverviewRes;
import com.genesis.unipocket.analysis.query.service.AnalysisMonthlySummaryQueryService;
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

	private final AnalysisMonthlySummaryQueryService analysisMonthlySummaryQueryService;

	@Operation(summary = "가계부 분석 전체 요약 조회", description = "종합적인 가계부 분석 데이터를 조회합니다.")
	@GetMapping("/account-books/{accountBookId}/analysis")
	public ResponseEntity<AnalysisOverviewRes> getAnalysisOverview(
			@LoginUser UUID userId,
			@PathVariable Long accountBookId,
			@RequestParam String year,
			@RequestParam String month,
			@RequestParam(name = "currencyType", defaultValue = "BASE") CurrencyType currencyType) {
		return ResponseEntity.ok(
				analysisMonthlySummaryQueryService.getAnalysisOverview(
						userId, accountBookId, year, month, currencyType));
	}
}
