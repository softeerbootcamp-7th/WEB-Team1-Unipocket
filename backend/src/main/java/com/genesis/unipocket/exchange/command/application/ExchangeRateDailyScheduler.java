package com.genesis.unipocket.exchange.command.application;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
		prefix = "exchange.scheduler",
		name = "enabled",
		havingValue = "true",
		matchIfMissing = true)
public class ExchangeRateDailyScheduler {

	private final ExchangeRateCommandService exchangeRateCommandService;
	private final AtomicBoolean running = new AtomicBoolean(false);

	@Scheduled(
			cron = "${exchange.scheduler.cron:0 20 0 * * *}",
			zone = "${exchange.scheduler.zone:UTC}")
	public void preloadDailyUsdRelativeRates() {
		if (!running.compareAndSet(false, true)) {
			return;
		}

		LocalDate targetDate = LocalDate.now(ZoneOffset.UTC).minusDays(1);
		int successCount = 0;
		int failureCount = 0;
		try {
			for (CurrencyCode currencyCode : CurrencyCode.values()) {
				if (currencyCode == CurrencyCode.USD) {
					continue;
				}
				try {
					exchangeRateCommandService.resolveAndStoreUsdRelativeRate(
							currencyCode, targetDate);
					successCount++;
				} catch (Exception e) {
					failureCount++;
					log.warn(
							"Daily exchange preload failed (stacktrace follows). currency={}, targetDate={}",
							currencyCode,
							targetDate,
							e);
				}
			}
			log.info(
					"Daily exchange preload completed. targetDate={}, successCount={},"
							+ " failureCount={}",
					targetDate,
					successCount,
					failureCount);
		} finally {
			running.set(false);
		}
	}
}
