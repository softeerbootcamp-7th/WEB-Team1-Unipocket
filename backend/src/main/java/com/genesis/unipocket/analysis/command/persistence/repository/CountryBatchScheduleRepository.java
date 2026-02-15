package com.genesis.unipocket.analysis.command.persistence.repository;

import com.genesis.unipocket.analysis.command.persistence.entity.CountryBatchScheduleEntity;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CountryBatchScheduleRepository
		extends JpaRepository<CountryBatchScheduleEntity, CountryCode> {

	@Query(
			"""
			SELECT s
			FROM CountryBatchScheduleEntity s
			WHERE s.nextRunAtUtc <= :nowUtc
			""")
	List<CountryBatchScheduleEntity> findDueSchedules(@Param("nowUtc") LocalDateTime nowUtc);
}
