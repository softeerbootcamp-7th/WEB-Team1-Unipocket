package com.genesis.unipocket.analysis.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.analysis.command.facade.port.AnalysisAccountBookReadService;
import com.genesis.unipocket.analysis.command.facade.port.AnalysisExpenseReadService;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisMonthlyDirtyEntity;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisMonthlyDirtyRepository;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalysisMonthlyDirtyMarkerServiceTest {

	@Mock private AnalysisAccountBookReadService analysisAccountBookReadService;
	@Mock private AnalysisExpenseReadService analysisExpenseReadService;
	@Mock private AnalysisMonthlyDirtyRepository monthlyDirtyRepository;

	@InjectMocks private AnalysisMonthlyDirtyMarkerService service;

	@Test
	@DisplayName("동일 월 이벤트가 여러 개여도 월별 dirty는 1건만 저장된다")
	void markDirty_deduplicatesByMonth() {
		Long accountBookId = 1L;
		when(analysisAccountBookReadService.getRequiredCountryInfo(accountBookId))
				.thenReturn(
						new AnalysisAccountBookReadService.AccountBookCountryInfo(
								accountBookId, CountryCode.KR, CountryCode.KR));
		when(monthlyDirtyRepository.findByCountryCodeAndAccountBookIdAndTargetYearMonth(
						eq(CountryCode.KR), eq(accountBookId), any(LocalDate.class)))
				.thenReturn(Optional.empty());

		Set<OffsetDateTime> occurredAts = new LinkedHashSet<>();
		occurredAts.add(OffsetDateTime.parse("2026-02-01T10:00:00+09:00"));
		occurredAts.add(OffsetDateTime.parse("2026-02-20T08:00:00+09:00"));
		occurredAts.add(OffsetDateTime.parse("2026-03-01T09:00:00+09:00"));

		service.markDirty(accountBookId, occurredAts);

		ArgumentCaptor<LocalDate> monthCaptor = ArgumentCaptor.forClass(LocalDate.class);
		verify(monthlyDirtyRepository, org.mockito.Mockito.times(2))
				.findByCountryCodeAndAccountBookIdAndTargetYearMonth(
						eq(CountryCode.KR), eq(accountBookId), monthCaptor.capture());
		verify(monthlyDirtyRepository, org.mockito.Mockito.times(2))
				.save(any(AnalysisMonthlyDirtyEntity.class));
		assertThat(monthCaptor.getAllValues())
				.containsExactly(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 1));
	}

	@Test
	@DisplayName("기존 dirty row가 있으면 해당 엔티티를 갱신해서 저장한다")
	void markDirty_updatesExistingDirty() {
		Long accountBookId = 2L;
		when(analysisAccountBookReadService.getRequiredCountryInfo(accountBookId))
				.thenReturn(
						new AnalysisAccountBookReadService.AccountBookCountryInfo(
								accountBookId, CountryCode.KR, CountryCode.KR));

		AnalysisMonthlyDirtyEntity existing =
				AnalysisMonthlyDirtyEntity.create(
						CountryCode.KR,
						accountBookId,
						LocalDate.of(2026, 4, 1),
						LocalDateTime.of(2026, 4, 2, 0, 0));
		when(monthlyDirtyRepository.findByCountryCodeAndAccountBookIdAndTargetYearMonth(
						CountryCode.KR, accountBookId, LocalDate.of(2026, 4, 1)))
				.thenReturn(Optional.of(existing));

		service.markDirty(accountBookId, OffsetDateTime.parse("2026-04-15T09:00:00+09:00"));

		ArgumentCaptor<AnalysisMonthlyDirtyEntity> entityCaptor =
				ArgumentCaptor.forClass(AnalysisMonthlyDirtyEntity.class);
		verify(monthlyDirtyRepository).save(entityCaptor.capture());
		assertThat(entityCaptor.getValue()).isSameAs(existing);
	}

	@Test
	@DisplayName("이벤트 입력이 비어있으면 dirty 저장을 시도하지 않는다")
	void markDirty_emptyInput_noop() {
		service.markDirty(1L, Set.of());

		verify(analysisAccountBookReadService, never()).getRequiredCountryInfo(any());
		verify(monthlyDirtyRepository, never()).save(any());
	}

	@Test
	@DisplayName("가계부 전체 월 dirty 마킹은 최소/최대 발생월 범위를 모두 생성한다")
	void markDirtyAllMonths_marksFullMonthRange() {
		Long accountBookId = 3L;
		when(analysisAccountBookReadService.getRequiredCountryInfo(accountBookId))
				.thenReturn(
						new AnalysisAccountBookReadService.AccountBookCountryInfo(
								accountBookId, CountryCode.KR, CountryCode.KR));
		when(analysisExpenseReadService.findOccurredAtRange(accountBookId))
				.thenReturn(
						Optional.of(
								new AnalysisExpenseReadService.ExpenseOccurredRange(
										OffsetDateTime.parse("2026-01-05T10:00:00+09:00"),
										OffsetDateTime.parse("2026-03-17T10:00:00+09:00"))));
		when(monthlyDirtyRepository.findByCountryCodeAndAccountBookIdAndTargetYearMonth(
						eq(CountryCode.KR), eq(accountBookId), any(LocalDate.class)))
				.thenReturn(Optional.empty());

		service.markDirtyAllMonths(accountBookId);

		ArgumentCaptor<LocalDate> monthCaptor = ArgumentCaptor.forClass(LocalDate.class);
		verify(monthlyDirtyRepository, org.mockito.Mockito.times(3))
				.findByCountryCodeAndAccountBookIdAndTargetYearMonth(
						eq(CountryCode.KR), eq(accountBookId), monthCaptor.capture());
		assertThat(monthCaptor.getAllValues())
				.containsExactly(
						LocalDate.of(2026, 1, 1),
						LocalDate.of(2026, 2, 1),
						LocalDate.of(2026, 3, 1));
	}
}
