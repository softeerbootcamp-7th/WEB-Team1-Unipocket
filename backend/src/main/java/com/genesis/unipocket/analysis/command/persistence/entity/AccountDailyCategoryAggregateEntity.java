package com.genesis.unipocket.analysis.command.persistence.entity;

import com.genesis.unipocket.global.common.entity.BaseEntity;
import com.genesis.unipocket.global.common.enums.Category;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
		name = "account_daily_category_aggregate",
		uniqueConstraints = {
			@UniqueConstraint(
					name = "uk_account_daily_category_aggregate",
					columnNames = {
						"account_book_id",
						"target_local_date",
						"category",
						"quality_type"
					})
		})
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountDailyCategoryAggregateEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false)
	private Long id;

	@Column(nullable = false, name = "account_book_id")
	private Long accountBookId;

	@Column(nullable = false, name = "target_local_date")
	private LocalDate targetLocalDate;

	@Column(nullable = false, name = "category", length = 32)
	@Enumerated(EnumType.STRING)
	private Category category;

	@Column(nullable = false, name = "quality_type", length = 16)
	@Enumerated(EnumType.STRING)
	private AnalysisQualityType qualityType;

	@Column(nullable = false, name = "total_amount", precision = 19, scale = 4)
	private BigDecimal totalAmount;

	@Column(nullable = false, name = "expense_count")
	private long expenseCount;

	public static AccountDailyCategoryAggregateEntity of(
			Long accountBookId,
			LocalDate targetLocalDate,
			Category category,
			AnalysisQualityType qualityType,
			BigDecimal totalAmount,
			long expenseCount) {
		return AccountDailyCategoryAggregateEntity.builder()
				.accountBookId(accountBookId)
				.targetLocalDate(targetLocalDate)
				.category(category)
				.qualityType(qualityType)
				.totalAmount(totalAmount)
				.expenseCount(expenseCount)
				.build();
	}

	public void update(BigDecimal totalAmount, long expenseCount) {
		this.totalAmount = totalAmount;
		this.expenseCount = expenseCount;
	}
}
