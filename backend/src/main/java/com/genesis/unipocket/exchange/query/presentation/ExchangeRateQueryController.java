package com.genesis.unipocket.exchange.query.presentation;

import com.genesis.unipocket.exchange.query.application.ExchangeRateService;
import com.genesis.unipocket.exchange.query.presentation.response.ExchangeRateQuoteResponse;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "환율 조회 API")
@RestController
@RequiredArgsConstructor
public class ExchangeRateQueryController {

	private final ExchangeRateService exchangeRateService;

	@Operation(
			summary = "통화 간 환율 조회",
			description = "occurredAt 시점 기준으로 baseCurrencyCode -> localCurrencyCode 환율을 조회합니다.")
	@GetMapping("/exchange-rate")
	public ResponseEntity<ExchangeRateQuoteResponse> getExchangeRate(
			@RequestParam OffsetDateTime occurredAt,
			@RequestParam CurrencyCode baseCurrencyCode,
			@RequestParam CurrencyCode localCurrencyCode) {
		return ResponseEntity.ok(
				new ExchangeRateQuoteResponse(
						occurredAt,
						baseCurrencyCode,
						localCurrencyCode,
						exchangeRateService.getExchangeRate(
								baseCurrencyCode, localCurrencyCode, occurredAt)));
	}
}
