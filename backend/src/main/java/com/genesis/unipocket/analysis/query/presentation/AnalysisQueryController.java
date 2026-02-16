package com.genesis.unipocket.analysis.query.presentation;

import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import com.genesis.unipocket.analysis.query.persistence.response.CategoryBreakdownRes;
import com.genesis.unipocket.analysis.query.persistence.response.MonthlySpendSummaryRes;
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

	@Operation(summary = "월 지출 요약 조회", description = "이번달/전달 일자별(CDF) 지출과 남들 평균 대비 차이를 조회합니다.")
	@GetMapping("/account-books/{accountBookId}/analysis/monthly-summary")
	public ResponseEntity<MonthlySpendSummaryRes> getMonthlySpendSummary(
			@LoginUser UUID userId,
			@PathVariable Long accountBookId,
			@RequestParam int year,
			@RequestParam String month,
			@RequestParam(name = "currencyView", defaultValue = "BASE") CurrencyType currencyView) {
		return ResponseEntity.ok(
				analysisMonthlySummaryQueryService.getMonthlySpendSummary(
						userId, accountBookId, year, month, currencyView));
	}

	@Operation(summary = "카테고리 지출 분해 조회", description = "카테고리별 내 지출/남들 평균/차이를 조회합니다.")
	@GetMapping("/account-books/{accountBookId}/analysis/category-breakdown")
	public ResponseEntity<CategoryBreakdownRes> getCategoryBreakdown(
			@LoginUser UUID userId,
			@PathVariable Long accountBookId,
			@RequestParam int year,
			@RequestParam String month,
			@RequestParam(name = "currencyView", defaultValue = "BASE") CurrencyType currencyView) {
		return ResponseEntity.ok(
				analysisMonthlySummaryQueryService.getCategoryBreakdown(
						userId, accountBookId, year, month, currencyView));
	}
}
