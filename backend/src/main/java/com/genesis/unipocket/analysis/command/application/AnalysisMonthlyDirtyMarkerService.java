package com.genesis.unipocket.analysis.command.application;

import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.command.persistence.repository.AccountBookCommandRepository;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalysisMonthlyDirtyMarkerService {

	private final AccountBookCommandRepository accountBookRepository;
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

	private void markDirty(Long accountBookId, Set<OffsetDateTime> occurredAts) {
		AccountBookEntity accountBook =
				accountBookRepository
						.findById(accountBookId)
						.orElseThrow(
								() ->
										new IllegalStateException(
												"Account book not found for dirty marking: "
														+ accountBookId));

		CountryCode localCountry = accountBook.getLocalCountryCode();
		CountryCode baseCountry = accountBook.getBaseCountryCode();
		if (localCountry == null || baseCountry == null || localCountry != baseCountry) {
			return;
		}

		ZoneId zoneId = CountryCodeTimezoneMapper.getZoneId(localCountry);
		LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
		for (OffsetDateTime occurredAt : occurredAts) {
			LocalDate targetYearMonth =
					occurredAt.atZoneSameInstant(zoneId).toLocalDate().withDayOfMonth(1);
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
