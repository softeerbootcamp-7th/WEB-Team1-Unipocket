package com.genesis.unipocket.travel.command.persistence.entity;

import com.genesis.unipocket.global.common.entity.BaseEntity;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "travel")
public class Travel extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "travel_id")
	private Long id;

	@Column(nullable = false)
	private Long accountBookId;

	@Column(name = "image_key")
	private String imageKey;

	@Column(nullable = false)
	private LocalDate startDate;

	@Column(nullable = false)
	private LocalDate endDate;

	@Column(nullable = false)
	private String travelPlaceName;

	public void validateDateRange() {
		if (startDate.isAfter(endDate)) {
			throw new BusinessException(ErrorCode.TRAVEL_INVALID_DATE_RANGE);
		}
	}

	public void update(
			String travelPlaceName, LocalDate startDate, LocalDate endDate, String imageKey) {
		this.travelPlaceName = travelPlaceName;
		this.startDate = startDate;
		this.endDate = endDate;
		this.imageKey = imageKey;
		validateDateRange();
	}

	public void updateImage(String imageKey) {
		this.imageKey = imageKey;
	}

	public void updateName(String travelPlaceName) {
		this.travelPlaceName = travelPlaceName;
	}

	public void updatePeriod(LocalDate startDate, LocalDate endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
		validateDateRange();
	}
}
