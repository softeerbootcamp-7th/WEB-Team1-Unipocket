package com.genesis.unipocket.accountbook.entity;

import com.genesis.unipocket.global.base.BaseEntity;
import com.genesis.unipocket.global.common.enums.CountryCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
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

	// TODO: User 모델을 받으면 새롭게 주입 예정
	@Column(nullable = false, name = "user_id")
	private Long userId;

	@Column(columnDefinition = "CHAR(4)")
	@Enumerated(value = EnumType.STRING)
	private CountryCode localCountryCode;

	@Column(columnDefinition = "CHAR(4)")
	@Enumerated(value = EnumType.STRING)
	private CountryCode baseCountryCode;

	@Column(nullable = false, length = 255)
	private String title;

	@Column(nullable = true)
	private Integer budget;

	@Column(name = "start_date", nullable = false, columnDefinition = "DATE")
	private LocalDate startDate;

	@Column(name = "end_date", nullable = false, columnDefinition = "DATE")
	private LocalDate endDate;

	public static AccountBookEntity create(
			Long userId,
			String title,
			CountryCode localCountryCode,
			CountryCode baseCountryCode,
			LocalDate startDate,
			LocalDate endDate) {

		return AccountBookEntity.builder()
				.userId(userId)
				.localCountryCode(localCountryCode)
				.baseCountryCode(baseCountryCode)
				.title(title)
				.startDate(startDate)
				.endDate(endDate)
				.build();
	}

	public void updateTitle(String title) {
		this.title = title;
	}

	public void changeAccountBookPeriod(LocalDate startDate, LocalDate endDate) {

		this.startDate = startDate;
		this.endDate = endDate;
	}

	public void updateBudget(int budget) {
		this.budget = budget;
	}

	public void resetBudget() {
		this.budget = null;
	}

	public boolean isBudgetSet() {
		return this.budget != null;
	}
}
