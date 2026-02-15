package com.genesis.unipocket.analysis.command.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.genesis.unipocket.analysis.command.config.AnalysisBatchProperties;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.ExpenseRow;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CountryDailyOutlierAggregationService 단위 테스트")
class CountryDailyOutlierAggregationServiceTest {

	@Test
	@DisplayName("샘플 수가 최소 기준보다 작으면 이상치 완화를 적용하지 않는다")
	void doesNotWinsorizeWhenSampleSizeIsSmall() {
		AnalysisBatchProperties properties = new AnalysisBatchProperties();
		properties.setOutlierMinSampleSize(30);
		CountryDailyOutlierAggregationService service =
				new CountryDailyOutlierAggregationService(properties);

		LocalDate targetDate = LocalDate.of(2026, 2, 10);
		LocalDateTime startUtc = LocalDateTime.of(2026, 2, 9, 15, 0);
		LocalDateTime endUtc = LocalDateTime.of(2026, 2, 10, 15, 0);

		List<ExpenseRow> rows = new ArrayList<>();
		for (long i = 1; i <= 9; i++) {
			rows.add(row(i, 1L, 2, BigDecimal.valueOf(100), "KRW", startUtc.plusHours(1)));
		}
		rows.add(row(10L, 1L, 2, BigDecimal.valueOf(10000), "KRW", startUtc.plusHours(2)));

		var result =
				service.calculate(
						CountryCode.KR, targetDate, startUtc, endUtc, rows, rows, List.of(1L));

		assertThat(result.countryAmountCount().totalAmount()).isEqualByComparingTo("10900");
		assertThat(result.countryAmountCount().expenseCount()).isEqualTo(10);
		assertThat(result.audits())
				.anyMatch(
						audit ->
								"SOFT".equals(audit.getRuleType())
										&& "FLAG_ONLY".equals(audit.getAction()));
		assertThat(result.audits()).noneMatch(audit -> "WINSORIZE_HIGH".equals(audit.getAction()));
	}

	@Test
	@DisplayName("샘플 수가 충분하면 IQR 경계를 사용해 상단 이상치를 winsorize 한다")
	void winsorizeHighOutlierWithIqr() {
		AnalysisBatchProperties properties = new AnalysisBatchProperties();
		properties.setOutlierMinSampleSize(30);
		properties.setOutlierMethod(AnalysisBatchProperties.OutlierMethod.IQR);
		CountryDailyOutlierAggregationService service =
				new CountryDailyOutlierAggregationService(properties);

		LocalDate targetDate = LocalDate.of(2026, 2, 10);
		LocalDateTime startUtc = LocalDateTime.of(2026, 2, 9, 15, 0);
		LocalDateTime endUtc = LocalDateTime.of(2026, 2, 10, 15, 0);

		List<ExpenseRow> rows = new ArrayList<>();
		for (long i = 1; i <= 29; i++) {
			rows.add(row(i, 1L, 1, BigDecimal.valueOf(100), "KRW", startUtc.plusHours(1)));
		}
		rows.add(row(30L, 1L, 1, BigDecimal.valueOf(10000), "KRW", startUtc.plusHours(2)));

		var result =
				service.calculate(
						CountryCode.KR, targetDate, startUtc, endUtc, rows, rows, List.of(1L));

		assertThat(result.countryAmountCount().totalAmount()).isEqualByComparingTo("3000");
		assertThat(result.countryAmountCount().expenseCount()).isEqualTo(30);
		assertThat(result.audits())
				.anyMatch(
						audit ->
								"SOFT".equals(audit.getRuleType())
										&& "WINSORIZE_HIGH".equals(audit.getAction()));
	}

	@Test
	@DisplayName("하드 정합성 실패 데이터는 cleaned 집계에서 제외한다")
	void dropsHardInvalidRows() {
		AnalysisBatchProperties properties = new AnalysisBatchProperties();
		properties.setOutlierMinSampleSize(2);
		CountryDailyOutlierAggregationService service =
				new CountryDailyOutlierAggregationService(properties);

		LocalDate targetDate = LocalDate.of(2026, 2, 10);
		LocalDateTime startUtc = LocalDateTime.of(2026, 2, 9, 15, 0);
		LocalDateTime endUtc = LocalDateTime.of(2026, 2, 10, 15, 0);

		List<ExpenseRow> rows =
				List.of(
						row(1L, 1L, 2, BigDecimal.valueOf(100), "KRW", startUtc.plusHours(1)),
						row(2L, 1L, 2, BigDecimal.valueOf(120), "USD", startUtc.plusHours(2)));

		var result =
				service.calculate(
						CountryCode.KR, targetDate, startUtc, endUtc, rows, rows, List.of(1L));

		assertThat(result.countryAmountCount().totalAmount()).isEqualByComparingTo("100");
		assertThat(result.countryAmountCount().expenseCount()).isEqualTo(1);
		assertThat(result.audits())
				.anyMatch(
						audit ->
								"HARD".equals(audit.getRuleType())
										&& "COUNTRY_CURRENCY_MISMATCH".equals(audit.getRuleName())
										&& "DROP".equals(audit.getAction()));
	}

	private ExpenseRow row(
			Long expenseId,
			Long accountBookId,
			Object category,
			BigDecimal amount,
			String currencyCode,
			LocalDateTime occurredAtUtc) {
		return new ExpenseRow(
				expenseId,
				accountBookId,
				category,
				amount,
				currencyCode,
				occurredAtUtc,
				"KR",
				"KR");
	}
}
