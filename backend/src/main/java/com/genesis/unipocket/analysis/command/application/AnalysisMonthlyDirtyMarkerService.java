package com.genesis.unipocket.analysis.command.application;

import com.genesis.unipocket.analysis.command.facade.port.AnalysisAccountBookReadService;
import com.genesis.unipocket.analysis.command.facade.port.AnalysisExpenseReadService;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisMonthlyDirtyEntity;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisMonthlyDirtyRepository;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.util.CountryCodeTimezoneMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalysisMonthlyDirtyMarkerService {

	private final AnalysisAccountBookReadService analysisAccountBookReadService;
	private final AnalysisExpenseReadService analysisExpenseReadService;
	private final AnalysisMonthlyDirtyRepository monthlyDirtyRepository;

	@Transactional
	public void markDirty(Long accountBookId, OffsetDateTime occurredAt) {
		if (occurredAt == null) {
			return;
		}
		markDirty(accountBookId, Set.of(occurredAt));
	}

	@Transactional
	public void markDirty(Long accountBookId, OffsetDateTime first, OffsetDateTime second) {
		Set<OffsetDateTime> occurredAts = new LinkedHashSet<>();
		if (first != null) {
			occurredAts.add(first);
		}
		if (second != null) {
			occurredAts.add(second);
		}
		if (occurredAts.isEmpty()) {
			return;
		}
		markDirty(accountBookId, occurredAts);
	}

	@Transactional
	public void markDirty(Long accountBookId, Set<OffsetDateTime> occurredAts) {
		if (occurredAts == null || occurredAts.isEmpty()) {
			return;
		}
		CountryCode localCountry =
				analysisAccountBookReadService.getRequiredCountryInfo(accountBookId).localCountryCode();
		ZoneId zoneId = CountryCodeTimezoneMapper.getZoneId(localCountry);
		LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
		Set<LocalDate> targetYearMonths =
				occurredAts.stream()
						.map(occurredAt -> occurredAt.atZoneSameInstant(zoneId).toLocalDate())
						.map(date -> date.withDayOfMonth(1))
						.collect(Collectors.toCollection(LinkedHashSet::new));
		upsertDirtyRows(localCountry, accountBookId, targetYearMonths, nowUtc);
	}

	@Transactional
	public void markDirtyAllMonths(Long accountBookId) {
		var occurredRange = analysisExpenseReadService.findOccurredAtRange(accountBookId);
		if (occurredRange.isEmpty()) {
			return;
		}
		OffsetDateTime minOccurredAt = occurredRange.get().minOccurredAt();
		OffsetDateTime maxOccurredAt = occurredRange.get().maxOccurredAt();

		CountryCode localCountry =
				analysisAccountBookReadService.getRequiredCountryInfo(accountBookId).localCountryCode();
		ZoneId zoneId = CountryCodeTimezoneMapper.getZoneId(localCountry);
		LocalDate startMonth =
				minOccurredAt.atZoneSameInstant(zoneId).toLocalDate().withDayOfMonth(1);
		LocalDate endMonth =
				maxOccurredAt.atZoneSameInstant(zoneId).toLocalDate().withDayOfMonth(1);

		Set<LocalDate> targetYearMonths = new LinkedHashSet<>();
		for (LocalDate month = startMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {
			targetYearMonths.add(month);
		}
		LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
		upsertDirtyRows(localCountry, accountBookId, targetYearMonths, nowUtc);
	}

	private void upsertDirtyRows(
			CountryCode localCountry,
			Long accountBookId,
			Set<LocalDate> targetYearMonths,
			LocalDateTime nowUtc) {
		for (LocalDate targetYearMonth : targetYearMonths) {
			AnalysisMonthlyDirtyEntity dirty =
					monthlyDirtyRepository
							.findByCountryCodeAndAccountBookIdAndTargetYearMonth(
									localCountry, accountBookId, targetYearMonth)
							.orElseGet(
									() ->
											AnalysisMonthlyDirtyEntity.create(
													localCountry,
													accountBookId,
													targetYearMonth,
													nowUtc));
			dirty.markPendingFromEvent(nowUtc);
			monthlyDirtyRepository.save(dirty);
		}
	}
}
