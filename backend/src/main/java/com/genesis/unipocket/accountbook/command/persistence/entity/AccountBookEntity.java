package com.genesis.unipocket.accountbook.command.persistence.entity;

import com.genesis.unipocket.global.common.entity.BaseEntity;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "account_book")
@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountBookEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false, name = "account_book_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;

	@Column(columnDefinition = "CHAR(4)")
	@Enumerated(value = EnumType.STRING)
	private CountryCode localCountryCode;

	@Column(columnDefinition = "CHAR(4)")
	@Enumerated(value = EnumType.STRING)
	private CountryCode baseCountryCode;

	@Column(nullable = false, length = 255)
	private String title;

	@Column(name = "bucket_order", nullable = false)
	private Integer bucketOrder;

	@Column(nullable = true, precision = 19, scale = 2)
	private BigDecimal budget;

	@Column(name = "budget_created_at")
	private LocalDateTime budgetCreatedAt;

	@Column(name = "budget_created_at")
	private LocalDateTime budgetCreatedAt;

	@Column(name = "start_date", nullable = false, columnDefinition = "DATE")
	private LocalDate startDate;

	@Column(name = "end_date", nullable = false, columnDefinition = "DATE")
	private LocalDate endDate;

	public static AccountBookEntity create(AccountBookCreateArgs args) {

		return AccountBookEntity.builder()
				.user(args.user())
				.localCountryCode(args.localCountryCode())
				.baseCountryCode(args.baseCountryCode())
				.title(args.title())
				.bucketOrder(args.bucketOrder())
				.budget(args.budget())
				.budgetCreatedAt(args.budget() == null ? null : LocalDateTime.now())
				.startDate(args.startDate())
				.endDate(args.endDate())
				.build();
	}

	public void updateTitle(String title) {
		this.title = title;
	}

	public void changeAccountBookPeriod(LocalDate startDate, LocalDate endDate) {

		this.startDate = startDate;
		this.endDate = endDate;
	}

	public void updateCountryCodes(CountryCode localCountryCode, CountryCode baseCountryCode) {
		this.localCountryCode = localCountryCode;
		this.baseCountryCode = baseCountryCode;
	}

	public void updateBudget(BigDecimal budget) {
		this.budget = budget;
		this.budgetCreatedAt = budget == null ? null : LocalDateTime.now();
	}

	public void resetBudget() {
		this.budget = null;
		this.budgetCreatedAt = null;
	}

	public boolean isBudgetSet() {
		return this.budget != null;
	}
}
