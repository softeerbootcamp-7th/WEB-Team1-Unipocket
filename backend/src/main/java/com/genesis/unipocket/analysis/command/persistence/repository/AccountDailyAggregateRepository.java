package com.genesis.unipocket.analysis.command.persistence.repository;

import com.genesis.unipocket.analysis.command.persistence.entity.AccountDailyAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisMetricType;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisQualityType;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountDailyAggregateRepository
		extends JpaRepository<AccountDailyAggregateEntity, Long> {

	Optional<AccountDailyAggregateEntity>
			findByAccountBookIdAndTargetLocalDateAndMetricTypeAndQualityType(
					Long accountBookId,
					LocalDate targetLocalDate,
					AnalysisMetricType metricType,
					AnalysisQualityType qualityType);
}
