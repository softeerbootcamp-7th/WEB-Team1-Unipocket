package com.genesis.unipocket.travel.dto.request;

import java.time.LocalDate;

/**
 * 여행 정보 부분 수정을 위한 DTO
 * 모든 필드는 Optional입니다. (null일 경우 수정하지 않음)
 */
public record TravelUpdateRequest(
		String travelPlaceName, LocalDate startDate, LocalDate endDate, String imageKey) {}
