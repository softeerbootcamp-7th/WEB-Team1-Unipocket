package com.genesis.unipocket.accountbook.facade.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.genesis.unipocket.accountbook.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

/**
 * <b>가계부 공통 DTO</b>
 * <p>
 * 여러 퍼사드 계층에서 통신할 때 사용
 * </p>
 * @author bluefishez
 * @since 2026-01-30
 */
@Builder
@Getter
public class AccountBookDto {

	private Long id;

	private Long userId;

	private String title;

	private CountryCode localCountryCode;

	private CountryCode baseCountryCode;

	private Integer budget;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private final LocalDate startDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private final LocalDate endDate;

	public static AccountBookDto from(AccountBookEntity entity) {
		var builder =
				AccountBookDto.builder()
						.id(entity.getId())
						.userId(entity.getUserId())
						.title(entity.getTitle())
						.localCountryCode(entity.getLocalCountryCode())
						.baseCountryCode(entity.getBaseCountryCode())
						.startDate(entity.getStartDate())
						.endDate(entity.getEndDate());

		if (entity.getBudget() != null) {
			builder.budget(entity.getBudget());
		}

		return builder.build();
	}
}
