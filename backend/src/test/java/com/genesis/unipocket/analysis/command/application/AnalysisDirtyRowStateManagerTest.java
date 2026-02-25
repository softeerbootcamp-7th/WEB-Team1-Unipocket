package com.genesis.unipocket.analysis.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.analysis.command.config.AnalysisBatchProperties;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisMonthlyDirtyEntity;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisMonthlyDirtyRepository;
import com.genesis.unipocket.analysis.common.enums.AnalysisBatchJobStatus;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalysisDirtyRowStateManagerTest {

	@Mock private AnalysisMonthlyDirtyRepository monthlyDirtyRepository;
	@Mock private AnalysisBatchProperties properties;

	@InjectMocks private AnalysisDirtyRowStateManager stateManager;

	private static final long DIRTY_ID = 42L;
	private static final LocalDateTime CLAIM_TIME = LocalDateTime.of(2025, 12, 1, 10, 0, 0);

	// ===== finalizeDirtyRun =====

	@Test
	@DisplayName("실행 중 새 이벤트가 발생했으면 PENDING으로 재대기하고 SUCCESS는 호출하지 않는다")
	void finalizeDirtyRun_newEventDuringRun_rependsOnly() {
		when(monthlyDirtyRepository.markPendingIfNewEventDuringRun(
						eq(DIRTY_ID),
						eq(AnalysisBatchJobStatus.RUNNING),
						eq(AnalysisBatchJobStatus.PENDING),
						eq(CLAIM_TIME)))
				.thenReturn(1);

		stateManager.finalizeDirtyRun(DIRTY_ID, CLAIM_TIME);

		verify(monthlyDirtyRepository)
				.markPendingIfNewEventDuringRun(
						DIRTY_ID,
						AnalysisBatchJobStatus.RUNNING,
						AnalysisBatchJobStatus.PENDING,
						CLAIM_TIME);
		verify(monthlyDirtyRepository, never())
				.markSuccessIfNoNewEventDuringRun(any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("실행 중 새 이벤트가 없으면 SUCCESS로 마킹한다")
	void finalizeDirtyRun_noNewEvent_marksSuccess() {
		when(monthlyDirtyRepository.markPendingIfNewEventDuringRun(
						eq(DIRTY_ID),
						eq(AnalysisBatchJobStatus.RUNNING),
						eq(AnalysisBatchJobStatus.PENDING),
						eq(CLAIM_TIME)))
				.thenReturn(0);

		stateManager.finalizeDirtyRun(DIRTY_ID, CLAIM_TIME);

		verify(monthlyDirtyRepository)
				.markSuccessIfNoNewEventDuringRun(
						eq(DIRTY_ID),
						eq(AnalysisBatchJobStatus.RUNNING),
						eq(AnalysisBatchJobStatus.SUCCESS),
						eq(CLAIM_TIME),
						any(LocalDateTime.class));
	}

	// ===== markFailure =====

	@Test
	@DisplayName("엔티티가 없으면 아무것도 하지 않는다")
	void markFailure_entityNotFound_noop() {
		when(monthlyDirtyRepository.findById(DIRTY_ID)).thenReturn(Optional.empty());

		stateManager.markFailure(DIRTY_ID, new RuntimeException("test"));

		verify(monthlyDirtyRepository).findById(DIRTY_ID);
	}

	@Test
	@DisplayName("상태가 RUNNING이 아닌 경우 상태 전환을 하지 않는다")
	void markFailure_statusNotRunning_noop() {
		AnalysisMonthlyDirtyEntity entity =
				AnalysisMonthlyDirtyEntity.create(
						CountryCode.KR,
						1L,
						LocalDate.of(2025, 12, 1),
						LocalDateTime.of(2025, 12, 1, 0, 0));
		// 기본 생성 상태는 PENDING
		assertThat(entity.getStatus()).isEqualTo(AnalysisBatchJobStatus.PENDING);

		when(monthlyDirtyRepository.findById(DIRTY_ID)).thenReturn(Optional.of(entity));

		stateManager.markFailure(DIRTY_ID, new RuntimeException("test"));

		// PENDING 상태에서는 아무것도 변경되지 않음
		assertThat(entity.getStatus()).isEqualTo(AnalysisBatchJobStatus.PENDING);
	}

	@Test
	@DisplayName("재시도 횟수 초과 시 DEAD로 마킹된다")
	void markFailure_exceedsMaxRetry_marksDead() {
		AnalysisMonthlyDirtyEntity runningEntity = makeRunningEntity(5);

		when(properties.getMaxRetry()).thenReturn(5);
		when(monthlyDirtyRepository.findById(DIRTY_ID)).thenReturn(Optional.of(runningEntity));

		RuntimeException ex = new RuntimeException("DB connection failed");
		stateManager.markFailure(DIRTY_ID, ex);

		assertThat(runningEntity.getStatus()).isEqualTo(AnalysisBatchJobStatus.DEAD);
		assertThat(runningEntity.getErrorCode()).isEqualTo("RuntimeException");
		assertThat(runningEntity.getErrorMessage()).isEqualTo("DB connection failed");
	}

	@Test
	@DisplayName("재시도 횟수 미만이면 RETRY로 마킹되고 지수 백오프 딜레이가 적용된다")
	void markFailure_belowMaxRetry_marksRetryWithExponentialBackoff() {
		// attempt=2일 때 delay = retryBaseMinutes * (1 << max(0, 2-1)) = 5 * 2 = 10분
		AnalysisMonthlyDirtyEntity runningEntity = makeRunningEntity(2);

		when(properties.getMaxRetry()).thenReturn(5);
		when(properties.getRetryBaseMinutes()).thenReturn(5);
		when(monthlyDirtyRepository.findById(DIRTY_ID)).thenReturn(Optional.of(runningEntity));

		LocalDateTime beforeCallUtc = LocalDateTime.now(ZoneOffset.UTC);
		stateManager.markFailure(DIRTY_ID, new RuntimeException("transient error"));
		LocalDateTime afterCallUtc = LocalDateTime.now(ZoneOffset.UTC);

		assertThat(runningEntity.getStatus()).isEqualTo(AnalysisBatchJobStatus.RETRY);
		assertThat(runningEntity.getNextRetryAtUtc())
				.isBetween(
						beforeCallUtc.plusMinutes(10).minusSeconds(1),
						afterCallUtc.plusMinutes(10).plusSeconds(1));
	}

	@Test
	@DisplayName("attempt=1일 때 delay는 retryBaseMinutes * 1 이다 (최소 딜레이)")
	void markFailure_firstAttempt_minimalDelay() {
		// attempt=1 → multiplier = 1 << max(0, 1-1) = 1 << 0 = 1
		AnalysisMonthlyDirtyEntity runningEntity = makeRunningEntity(1);

		when(properties.getMaxRetry()).thenReturn(5);
		when(properties.getRetryBaseMinutes()).thenReturn(10);
		when(monthlyDirtyRepository.findById(DIRTY_ID)).thenReturn(Optional.of(runningEntity));

		LocalDateTime beforeCallUtc = LocalDateTime.now(ZoneOffset.UTC);
		stateManager.markFailure(DIRTY_ID, new RuntimeException("err"));
		LocalDateTime afterCallUtc = LocalDateTime.now(ZoneOffset.UTC);

		assertThat(runningEntity.getStatus()).isEqualTo(AnalysisBatchJobStatus.RETRY);
		assertThat(runningEntity.getNextRetryAtUtc())
				.isBetween(
						beforeCallUtc.plusMinutes(10).minusSeconds(1),
						afterCallUtc.plusMinutes(10).plusSeconds(1));
	}

	@Test
	@DisplayName("에러 메시지가 1000자 초과이면 잘라서 저장한다")
	void markFailure_longErrorMessage_truncated() {
		AnalysisMonthlyDirtyEntity runningEntity = makeRunningEntity(1);

		when(properties.getMaxRetry()).thenReturn(5);
		when(properties.getRetryBaseMinutes()).thenReturn(1);
		when(monthlyDirtyRepository.findById(DIRTY_ID)).thenReturn(Optional.of(runningEntity));

		String longMessage = "x".repeat(2000);
		stateManager.markFailure(DIRTY_ID, new RuntimeException(longMessage));

		assertThat(runningEntity.getErrorMessage()).hasSize(1000);
	}

	/**
	 * attemptCount를 지정해 RUNNING 상태의 엔티티를 빌더로 직접 생성한다.
	 */
	private AnalysisMonthlyDirtyEntity makeRunningEntity(int attemptCount) {
		// AnalysisMonthlyDirtyEntity.builder()는 package-private이 아니므로 리플렉션 없이
		// create() 후 markRetry로 상태를 RETRY 로 바꾸고, claimDirty는 DB 레벨이라 여기선
		// 테스트 목적상 Mockito 스파이 또는 직접 빌더 사용
		// → 여기서는 백박스 테스트를 위해 리플렉션으로 status 주입
		var entity =
				AnalysisMonthlyDirtyEntity.create(
						CountryCode.KR,
						1L,
						LocalDate.of(2025, 12, 1),
						LocalDateTime.of(2025, 12, 1, 0, 0));
		// RUNNING 상태와 attemptCount를 리플렉션으로 설정
		try {
			var statusField = AnalysisMonthlyDirtyEntity.class.getDeclaredField("status");
			statusField.setAccessible(true);
			statusField.set(entity, AnalysisBatchJobStatus.RUNNING);

			var attemptField = AnalysisMonthlyDirtyEntity.class.getDeclaredField("attemptCount");
			attemptField.setAccessible(true);
			attemptField.set(entity, attemptCount);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return entity;
	}
}
