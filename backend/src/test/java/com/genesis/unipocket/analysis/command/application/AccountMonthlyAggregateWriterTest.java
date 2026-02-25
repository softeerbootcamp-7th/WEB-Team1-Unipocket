package com.genesis.unipocket.analysis.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.analysis.command.persistence.entity.AccountMonthlyAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.entity.AccountMonthlyCategoryAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.repository.AccountMonthlyAggregateRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.AccountMonthlyCategoryAggregateRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisBatchAggregationRepository.CategoryAmountPairCount;
import com.genesis.unipocket.analysis.common.enums.AnalysisMetricType;
import com.genesis.unipocket.analysis.common.enums.AnalysisQualityType;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountMonthlyAggregateWriterTest {

	@Mock private AccountMonthlyAggregateRepository accountMonthlyAggregateRepository;

	@Mock
	private AccountMonthlyCategoryAggregateRepository accountMonthlyCategoryAggregateRepository;

	@InjectMocks private AccountMonthlyAggregateWriter writer;

	private static final Long ACCOUNT_BOOK_ID = 1L;
	private static final CountryCode COUNTRY_CODE = CountryCode.KR;
	private static final LocalDate MONTH_START = LocalDate.of(2025, 12, 1);
	private static final AnalysisQualityType QUALITY = AnalysisQualityType.CLEANED;

	// ===== upsertMonthlyMetrics =====

	@Test
	@DisplayName("기존 집계가 없으면 LOCAL·BASE 메트릭 엔티티를 새로 저장한다")
	void upsertMonthlyMetrics_noExisting_savesBothMetrics() {
		when(accountMonthlyAggregateRepository
						.findByAccountBookIdAndTargetYearMonthAndMetricTypeAndQualityType(
								any(), any(), any(), any()))
				.thenReturn(Optional.empty());

		var amountCount =
				new com.genesis.unipocket.analysis.command.persistence.repository
						.AnalysisBatchAggregationRepository.AmountPairCount(
						new BigDecimal("10000"), new BigDecimal("100"), 3);

		writer.upsertMonthlyMetrics(
				ACCOUNT_BOOK_ID, COUNTRY_CODE, MONTH_START, amountCount, QUALITY);

		verify(accountMonthlyAggregateRepository, org.mockito.Mockito.times(2))
				.save(any(AccountMonthlyAggregateEntity.class));
	}

	@Test
	@DisplayName("기존 집계가 있으면 save 대신 updateMetricValue를 호출한다")
	void upsertMonthlyMetrics_existing_updatesMetricValue() {
		AccountMonthlyAggregateEntity existingLocal =
				AccountMonthlyAggregateEntity.of(
						ACCOUNT_BOOK_ID,
						COUNTRY_CODE,
						MONTH_START,
						AnalysisMetricType.TOTAL_LOCAL_AMOUNT,
						QUALITY,
						new BigDecimal("5000"));
		AccountMonthlyAggregateEntity existingBase =
				AccountMonthlyAggregateEntity.of(
						ACCOUNT_BOOK_ID,
						COUNTRY_CODE,
						MONTH_START,
						AnalysisMetricType.TOTAL_BASE_AMOUNT,
						QUALITY,
						new BigDecimal("50"));

		when(accountMonthlyAggregateRepository
						.findByAccountBookIdAndTargetYearMonthAndMetricTypeAndQualityType(
								ACCOUNT_BOOK_ID,
								MONTH_START,
								AnalysisMetricType.TOTAL_LOCAL_AMOUNT,
								QUALITY))
				.thenReturn(Optional.of(existingLocal));
		when(accountMonthlyAggregateRepository
						.findByAccountBookIdAndTargetYearMonthAndMetricTypeAndQualityType(
								ACCOUNT_BOOK_ID,
								MONTH_START,
								AnalysisMetricType.TOTAL_BASE_AMOUNT,
								QUALITY))
				.thenReturn(Optional.of(existingBase));

		var amountCount =
				new com.genesis.unipocket.analysis.command.persistence.repository
						.AnalysisBatchAggregationRepository.AmountPairCount(
						new BigDecimal("12000"), new BigDecimal("120"), 4);

		writer.upsertMonthlyMetrics(
				ACCOUNT_BOOK_ID, COUNTRY_CODE, MONTH_START, amountCount, QUALITY);

		verify(accountMonthlyAggregateRepository, never()).save(any());
		assertThat(existingLocal.getMetricValue()).isEqualByComparingTo("12000");
		assertThat(existingBase.getMetricValue()).isEqualByComparingTo("120");
	}

	// ===== upsertMonthlyCategoryMetrics =====

	@Test
	@DisplayName("빈 rows 목록이면 delete 후 saveAll은 호출하지 않는다")
	void upsertMonthlyCategoryMetrics_emptyRows_deletesOnly() {
		writer.upsertMonthlyCategoryMetrics(
				ACCOUNT_BOOK_ID, COUNTRY_CODE, MONTH_START, List.of(), QUALITY);

		verify(accountMonthlyCategoryAggregateRepository)
				.deleteByAccountBookIdAndTargetYearMonthAndQualityType(
						ACCOUNT_BOOK_ID, MONTH_START, QUALITY);
		verify(accountMonthlyCategoryAggregateRepository).flush();
		verify(accountMonthlyCategoryAggregateRepository, never()).saveAll(any());
	}

	@Test
	@DisplayName("rows가 있으면 각 row마다 LOCAL·BASE 2개씩 엔티티를 저장한다")
	void upsertMonthlyCategoryMetrics_withRows_savesLocalAndBasePerRow() {
		List<CategoryAmountPairCount> rows =
				List.of(
						new CategoryAmountPairCount(
								Category.FOOD.ordinal(),
								new BigDecimal("5000"),
								new BigDecimal("50"),
								2),
						new CategoryAmountPairCount(
								Category.LIVING.ordinal(),
								new BigDecimal("3000"),
								new BigDecimal("30"),
								1));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<AccountMonthlyCategoryAggregateEntity>> captor =
				ArgumentCaptor.forClass(List.class);

		writer.upsertMonthlyCategoryMetrics(
				ACCOUNT_BOOK_ID, COUNTRY_CODE, MONTH_START, rows, QUALITY);

		verify(accountMonthlyCategoryAggregateRepository).saveAll(captor.capture());
		// 2개 카테고리 × 2 (LOCAL + BASE) = 4개
		assertThat(captor.getValue()).hasSize(4);
	}

	@Test
	@DisplayName("유효하지 않은 category ordinal이면 IllegalArgumentException이 발생한다")
	void upsertMonthlyCategoryMetrics_invalidCategoryOrdinal_throwsIllegalArgument() {
		List<CategoryAmountPairCount> rows =
				List.of(
						new CategoryAmountPairCount(
								-1, new BigDecimal("1000"), new BigDecimal("10"), 1));

		assertThatThrownBy(
						() ->
								writer.upsertMonthlyCategoryMetrics(
										ACCOUNT_BOOK_ID, COUNTRY_CODE, MONTH_START, rows, QUALITY))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Invalid category ordinal");
	}

	@Test
	@DisplayName("category ordinal이 범위를 초과하면 IllegalArgumentException이 발생한다")
	void upsertMonthlyCategoryMetrics_outOfRangeCategoryOrdinal_throwsIllegalArgument() {
		List<CategoryAmountPairCount> rows =
				List.of(
						new CategoryAmountPairCount(
								9999, new BigDecimal("1000"), new BigDecimal("10"), 1));

		assertThatThrownBy(
						() ->
								writer.upsertMonthlyCategoryMetrics(
										ACCOUNT_BOOK_ID, COUNTRY_CODE, MONTH_START, rows, QUALITY))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Invalid category ordinal");
	}
}
