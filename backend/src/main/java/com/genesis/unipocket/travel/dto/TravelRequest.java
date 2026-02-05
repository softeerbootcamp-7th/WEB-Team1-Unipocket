package com.genesis.unipocket.travel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record TravelRequest(
		@NotNull(message = "가계부 ID는 필수입니다.") Long accountBookId,
		@NotBlank(message = "여행지 이름은 필수입니다.") String travelPlaceName,
		@NotNull(message = "시작일은 필수입니다.") LocalDate startDate,
		@NotNull(message = "종료일은 필수입니다.") LocalDate endDate,
		String imageKey) {}
