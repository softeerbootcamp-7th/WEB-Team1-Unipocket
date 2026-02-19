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

// 로깅 유틸 필드를 자동 생성한다.
@Slf4j
// 스프링 빈으로 등록한다.
@Component
// final 필드 기반 생성자를 자동 생성한다.
@RequiredArgsConstructor
// exchange.scheduler.enabled 설정이 true(또는 미지정)일 때만 활성화한다.
@ConditionalOnProperty(
		// exchange.scheduler.* 설정 묶음을 사용한다.
		prefix = "exchange.scheduler",
		// enabled 키를 조건으로 사용한다.
		name = "enabled",
		// 값이 true일 때 빈을 생성한다.
		havingValue = "true",
		// 설정이 없어도 기본 활성 상태로 본다.
		matchIfMissing = true)
// 일별 환율 선적재 스케줄러 구현체다.
public class ExchangeRateDailyScheduler {

	// 실제 환율 보정/저장 작업을 위임할 서비스다.
	private final ExchangeRateCommandService exchangeRateCommandService;
	// 동시에 두 번 실행되지 않도록 원자 플래그를 둔다.
	private final AtomicBoolean running = new AtomicBoolean(false);

	// 크론 스케줄로 전일 환율을 미리 적재한다.
	@Scheduled(
			// 기본값은 매일 22:00:00에 실행한다.
			cron = "${exchange.scheduler.cron:0 0 22 * * *}",
			// 기본 시간대는 UTC를 사용한다.
			zone = "${exchange.scheduler.zone:UTC}")
	// USD를 제외한 통화를 순회해 전일 환율을 캐시한다.
	public void preloadDailyUsdRelativeRates() {
		// 이미 실행 중이면 이번 트리거는 즉시 종료한다.
		if (!running.compareAndSet(false, true)) {
			return;
		}

		// Yahoo 일봉은 종가 기준이므로 UTC 기준 전일을 목표 일자로 잡는다.
		LocalDate targetDate = LocalDate.now(ZoneOffset.UTC).minusDays(1);
		// 성공 건수 집계를 위한 카운터다.
		int successCount = 0;
		// 실패 건수 집계를 위한 카운터다.
		int failureCount = 0;
		// 실행 종료 시 running 플래그를 반드시 해제하기 위해 try-finally를 사용한다.
		try {
			// 시스템 지원 통화 전체를 순회한다.
			for (CurrencyCode currencyCode : CurrencyCode.values()) {
				// USD는 기준 통화이므로 보정 대상에서 제외한다.
				if (currencyCode == CurrencyCode.USD) {
					continue;
				}
				// 통화별 예외 격리를 위해 내부 try-catch로 처리한다.
				try {
					// 해당 통화의 전일 환율을 보정/저장한다.
					exchangeRateCommandService.resolveAndStoreUsdRelativeRate(
							currencyCode, targetDate);
					// 성공 카운트를 1 증가시킨다.
					successCount++;
				} catch (Exception e) {
					// 실패 카운트를 1 증가시킨다.
					failureCount++;
					// 실패 통화/일자를 남기고 스택트레이스를 함께 기록한다.
					log.warn(
							"Daily exchange preload failed (stacktrace follows). currency={},"
									+ " targetDate={}",
							currencyCode,
							targetDate,
							e);
				}
			}
			// 전체 처리 요약 로그를 남긴다.
			log.info(
					"Daily exchange preload completed. targetDate={}, successCount={},"
							+ " failureCount={}",
					targetDate,
					successCount,
					failureCount);
		} finally {
			// 다음 스케줄 실행을 위해 running 플래그를 해제한다.
			running.set(false);
		}
	}
}
