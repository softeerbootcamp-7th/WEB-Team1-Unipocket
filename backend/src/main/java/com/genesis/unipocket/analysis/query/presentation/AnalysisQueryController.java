package com.genesis.unipocket.analysis.query.presentation;

import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisQualityType;
import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import com.genesis.unipocket.analysis.query.persistence.response.AccountBookAnalysisRes;
import com.genesis.unipocket.analysis.query.persistence.response.AccountBookMonthlyAggregateRes;
import com.genesis.unipocket.analysis.query.persistence.response.CountryMonthlyAggregateRes;
import com.genesis.unipocket.analysis.query.service.AnalysisAggregateQueryService;
import com.genesis.unipocket.analysis.query.service.AnalysisQueryService;
import com.genesis.unipocket.auth.common.annotation.LoginUser;
import com.genesis.unipocket.global.common.enums.CountryCode;
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
	private final AnalysisAggregateQueryService analysisAggregateQueryService;

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

	@Operation(summary = "국가 월간 집계 조회", description = "국가 기준 월간 집계(총액/건수/일별/카테고리)를 조회합니다.")
	@GetMapping("/analysis/countries/{countryCode}/monthly")
	public ResponseEntity<CountryMonthlyAggregateRes> getCountryMonthlyAggregate(
			@LoginUser UUID userId,
			@PathVariable CountryCode countryCode,
			@RequestParam int year,
			@RequestParam int month,
			@RequestParam(defaultValue = "CLEANED") AnalysisQualityType qualityType) {
		return ResponseEntity.ok(
				analysisAggregateQueryService.getCountryMonthlyAggregate(
						countryCode, year, month, qualityType));
	}

	@Operation(
			summary = "가계부 월간 집계 조회",
			description =
					"가계부 기준 월간 집계(총액/건수/일별/카테고리)를 조회합니다. (LocalCountry" + " == BaseCountry 대상)")
	@GetMapping("/account-books/{accountBookId}/analysis/monthly")
	public ResponseEntity<AccountBookMonthlyAggregateRes> getAccountBookMonthlyAggregate(
			@LoginUser UUID userId,
			@PathVariable Long accountBookId,
			@RequestParam int year,
			@RequestParam int month,
			@RequestParam(defaultValue = "CLEANED") AnalysisQualityType qualityType) {
		return ResponseEntity.ok(
				analysisAggregateQueryService.getAccountBookMonthlyAggregate(
						userId, accountBookId, year, month, qualityType));
	}
}
