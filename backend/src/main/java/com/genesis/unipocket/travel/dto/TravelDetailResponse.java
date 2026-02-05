package com.genesis.unipocket.travel.dto;

import com.genesis.unipocket.travel.domain.Travel;
import java.time.LocalDate;
import java.util.List;

public record TravelDetailResponse(
        Long travelId,
        Long accountBookId,
        String travelPlaceName,
        LocalDate startDate,
        LocalDate endDate,
        String imageKey,
        List<WidgetDto> widgets) {

    public static TravelDetailResponse of(Travel travel, List<WidgetDto> widgets) {
        return new TravelDetailResponse(
                travel.getId(),
                travel.getAccountBookId(),
                travel.getTravelPlaceName(),
                travel.getStartDate(),
                travel.getEndDate(),
                travel.getImageKey(),
                widgets);
    }
}
