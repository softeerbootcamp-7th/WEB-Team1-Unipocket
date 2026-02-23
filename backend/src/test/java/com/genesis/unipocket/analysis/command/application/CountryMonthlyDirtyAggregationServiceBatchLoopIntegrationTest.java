package com.genesis.unipocket.analysis.command.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.genesis.unipocket.TestcontainersConfiguration;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.command.persistence.repository.AccountBookCommandRepository;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisBatchJobStatus;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisMonthlyDirtyEntity;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisMonthlyDirtyRepository;
import com.genesis.unipocket.analysis.support.AnalysisFixtureFactory;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test-it")
@Import(TestcontainersConfiguration.class)
@TestPropertySource(
		properties = {
			"analysis.batch.peer-min-sample-size=1",
			"analysis.batch.dispatch-batch-size=2"
		})
@Transactional
@Tag("integration")
class CountryMonthlyDirtyAggregationServiceBatchLoopIntegrationTest {

	@Autowired private CountryMonthlyDirtyAggregationService service;
	@Autowired private UserCommandRepository userRepository;
	@Autowired private AccountBookCommandRepository accountBookRepository;
	@Autowired private AnalysisMonthlyDirtyRepository dirtyRepository;

	/**
	 * dispatchBatchSize=2 상태에서 dirty row가 5개일 때 while 루프가 반복(2+2+1)하며 모두 처리하는지 검증.
	 */
	@Test
	void processCountryDirtyRows_moreThanBatchSize_processesAllRows() {
		LocalDate monthStart = LocalDate.of(2026, 1, 1);
		UserEntity user = AnalysisFixtureFactory.saveUser(userRepository, "loop-batch");

		for (int i = 0; i < 5; i++) {
			AccountBookEntity book =
					AnalysisFixtureFactory.saveAccountBook(
							accountBookRepository,
							user,
							CountryCode.US,
							CountryCode.KR,
							"loop-book-" + i);
			AnalysisFixtureFactory.savePendingDirty(
					dirtyRepository, CountryCode.US, book.getId(), monthStart);
		}

		service.processCountryDirtyRows(CountryCode.US);

		assertThat(dirtyRepository.findAll())
				.hasSize(5)
				.allMatch(dirty -> dirty.getStatus() == AnalysisBatchJobStatus.SUCCESS);
	}

	/**
	 * lastEventAtUtc가 batchStartUtc보다 미래인 dirty row는 이번 배치에서 처리되지 않고 다음 배치로 위임되는지 검증.
	 */
	@Test
	void processCountryDirtyRows_futureLastEventAt_deferredToNextBatch() {
		LocalDate monthStart = LocalDate.of(2026, 1, 1);
		UserEntity user = AnalysisFixtureFactory.saveUser(userRepository, "future-event");

		AccountBookEntity pastBook =
				AnalysisFixtureFactory.saveAccountBook(
						accountBookRepository, user, CountryCode.US, CountryCode.KR, "past-book");
		AccountBookEntity futureBook =
				AnalysisFixtureFactory.saveAccountBook(
						accountBookRepository, user, CountryCode.US, CountryCode.KR, "future-book");

		// 과거 이벤트 → batchStartUtc(now) 이전이므로 이번 배치에서 처리
		AnalysisFixtureFactory.savePendingDirty(
				dirtyRepository, CountryCode.US, pastBook.getId(), monthStart);
		// 미래 이벤트 → batchStartUtc(now) 이후이므로 다음 배치로 위임
		AnalysisFixtureFactory.savePendingDirtyWithEventAt(
				dirtyRepository,
				CountryCode.US,
				futureBook.getId(),
				monthStart,
				LocalDateTime.of(2099, 1, 1, 0, 0));

		service.processCountryDirtyRows(CountryCode.US);

		AnalysisMonthlyDirtyEntity pastDirty =
				dirtyRepository
						.findByCountryCodeAndAccountBookIdAndTargetYearMonth(
								CountryCode.US, pastBook.getId(), monthStart)
						.orElseThrow();
		AnalysisMonthlyDirtyEntity futureDirty =
				dirtyRepository
						.findByCountryCodeAndAccountBookIdAndTargetYearMonth(
								CountryCode.US, futureBook.getId(), monthStart)
						.orElseThrow();

		assertThat(pastDirty.getStatus()).isEqualTo(AnalysisBatchJobStatus.SUCCESS);
		assertThat(futureDirty.getStatus()).isEqualTo(AnalysisBatchJobStatus.PENDING);
	}

	/**
	 * 모든 dirty row의 lastEventAtUtc가 batchStartUtc보다 미래일 때 아무것도 처리되지 않는지 검증.
	 */
	@Test
	void processCountryDirtyRows_allFutureLastEventAt_nothingProcessed() {
		LocalDate monthStart = LocalDate.of(2026, 1, 1);
		UserEntity user = AnalysisFixtureFactory.saveUser(userRepository, "all-future");

		for (int i = 0; i < 3; i++) {
			AccountBookEntity book =
					AnalysisFixtureFactory.saveAccountBook(
							accountBookRepository,
							user,
							CountryCode.US,
							CountryCode.KR,
							"future-book-" + i);
			AnalysisFixtureFactory.savePendingDirtyWithEventAt(
					dirtyRepository,
					CountryCode.US,
					book.getId(),
					monthStart,
					LocalDateTime.of(2099, 1, 1, 0, 0));
		}

		service.processCountryDirtyRows(CountryCode.US);

		assertThat(dirtyRepository.findAll())
				.hasSize(3)
				.allMatch(dirty -> dirty.getStatus() == AnalysisBatchJobStatus.PENDING);
	}
}
