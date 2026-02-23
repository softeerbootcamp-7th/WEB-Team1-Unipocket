package com.genesis.unipocket.analysis.command.persistence.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.genesis.unipocket.analysis.common.enums.AnalysisBatchJobStatus;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class AnalysisMonthlyDirtyEntityTest {

	@Test
	void markPendingFromEvent_afterRetry_resetsRetryFields() {
		AnalysisMonthlyDirtyEntity entity =
				AnalysisMonthlyDirtyEntity.create(
						CountryCode.KR,
						1L,
						LocalDate.of(2026, 2, 1),
						LocalDateTime.of(2026, 2, 1, 0, 0));
		entity.markRetry(
				LocalDateTime.of(2026, 2, 1, 1, 0), "RuntimeException", "temporary failure");

		LocalDateTime nowUtc = LocalDateTime.of(2026, 2, 2, 0, 0);
		entity.markPendingFromEvent(nowUtc);

		assertThat(entity.getStatus()).isEqualTo(AnalysisBatchJobStatus.PENDING);
		assertThat(entity.getNextRetryAtUtc()).isNull();
		assertThat(entity.getLeaseUntilUtc()).isNull();
		assertThat(entity.getErrorCode()).isNull();
		assertThat(entity.getErrorMessage()).isNull();
		assertThat(entity.getLastEventAtUtc()).isEqualTo(nowUtc);
	}

	@Test
	void markRetry_errorMessageTooLong_trimsToOneThousandChars() {
		AnalysisMonthlyDirtyEntity entity =
				AnalysisMonthlyDirtyEntity.create(
						CountryCode.US,
						2L,
						LocalDate.of(2026, 3, 1),
						LocalDateTime.of(2026, 3, 1, 0, 0));
		String longMessage = "a".repeat(1200);

		entity.markRetry(LocalDateTime.of(2026, 3, 1, 1, 0), "IllegalStateException", longMessage);

		assertThat(entity.getStatus()).isEqualTo(AnalysisBatchJobStatus.RETRY);
		assertThat(entity.getErrorMessage()).hasSize(1000);
	}

	@Test
	void markSuccess_afterFailure_resetsErrorFieldsAndMarksSuccess() {
		AnalysisMonthlyDirtyEntity entity =
				AnalysisMonthlyDirtyEntity.create(
						CountryCode.JP,
						3L,
						LocalDate.of(2026, 4, 1),
						LocalDateTime.of(2026, 4, 1, 0, 0));
		entity.markDead(
				LocalDateTime.of(2026, 4, 2, 0, 0), "IllegalArgumentException", "terminal failure");

		LocalDateTime successAt = LocalDateTime.of(2026, 4, 3, 0, 0);
		entity.markSuccess(successAt);

		assertThat(entity.getStatus()).isEqualTo(AnalysisBatchJobStatus.SUCCESS);
		assertThat(entity.getErrorCode()).isNull();
		assertThat(entity.getErrorMessage()).isNull();
		assertThat(entity.getNextRetryAtUtc()).isNull();
		assertThat(entity.getLeaseUntilUtc()).isNull();
		assertThat(entity.getLastEventAtUtc()).isEqualTo(successAt);
	}
}
