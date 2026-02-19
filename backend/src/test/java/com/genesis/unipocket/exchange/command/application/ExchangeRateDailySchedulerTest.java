package com.genesis.unipocket.exchange.command.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ExchangeRateDailySchedulerTest {

	private final ExchangeRateCommandService exchangeRateCommandService =
			Mockito.mock(ExchangeRateCommandService.class);
	private final ExchangeRateDailyScheduler scheduler =
			new ExchangeRateDailyScheduler(exchangeRateCommandService);

	@Test
	void preloadDailyUsdRelativeRates_run_expectedAllNonUsdCurrenciesInvoked() {
		LocalDate expectedTargetDate = LocalDate.now(ZoneOffset.UTC).minusDays(1);
		int expectedCount = CurrencyCode.values().length - 1;

		scheduler.preloadDailyUsdRelativeRates();

		verify(exchangeRateCommandService, times(expectedCount))
				.resolveAndStoreUsdRelativeRate(any(CurrencyCode.class), eq(expectedTargetDate));
	}

	@Test
	void preloadDailyUsdRelativeRates_whenOneCurrencyFails_expectedContinuesProcessing() {
		LocalDate expectedTargetDate = LocalDate.now(ZoneOffset.UTC).minusDays(1);
		doThrow(new RuntimeException("boom"))
				.when(exchangeRateCommandService)
				.resolveAndStoreUsdRelativeRate(eq(CurrencyCode.KRW), any(LocalDate.class));

		scheduler.preloadDailyUsdRelativeRates();

		verify(exchangeRateCommandService, atLeast(1))
				.resolveAndStoreUsdRelativeRate(eq(CurrencyCode.KRW), eq(expectedTargetDate));
		int expectedCount = CurrencyCode.values().length - 1;
		verify(exchangeRateCommandService, times(expectedCount))
				.resolveAndStoreUsdRelativeRate(any(CurrencyCode.class), eq(expectedTargetDate));
	}
}
