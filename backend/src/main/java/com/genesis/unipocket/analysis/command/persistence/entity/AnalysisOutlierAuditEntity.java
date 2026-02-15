package com.genesis.unipocket.analysis.command.persistence.entity;

import com.genesis.unipocket.global.common.entity.BaseEntity;
import com.genesis.unipocket.global.common.enums.CountryCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@Table(name = "analysis_outlier_audit")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisOutlierAuditEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false)
	private Long id;

	@Column(nullable = false, name = "country_code", length = 8)
	@Enumerated(EnumType.STRING)
	private CountryCode countryCode;

	@Column(nullable = false, name = "occurred_date")
	private LocalDate occurredDate;

	@Column(nullable = false, name = "record_key", length = 128)
	private String recordKey;

	@Column(nullable = false, name = "rule_type", length = 32)
	private String ruleType;

	@Column(nullable = false, name = "rule_name", length = 64)
	private String ruleName;

	@Column(name = "raw_value", precision = 19, scale = 4)
	private BigDecimal rawValue;

	@Column(name = "lower_bound", precision = 19, scale = 4)
	private BigDecimal lowerBound;

	@Column(name = "upper_bound", precision = 19, scale = 4)
	private BigDecimal upperBound;

	@Column(nullable = false, length = 32)
	private String action;
}
