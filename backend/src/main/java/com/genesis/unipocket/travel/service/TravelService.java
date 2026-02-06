package com.genesis.unipocket.travel.service;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.travel.domain.Travel;
import com.genesis.unipocket.travel.domain.TravelWidget;
import com.genesis.unipocket.travel.domain.WidgetType;
import com.genesis.unipocket.travel.dto.TravelDetailResponse;
import com.genesis.unipocket.travel.dto.TravelRequest;
import com.genesis.unipocket.travel.dto.TravelResponse;
import com.genesis.unipocket.travel.dto.WidgetDto;
import com.genesis.unipocket.travel.repository.TravelRepository;
import com.genesis.unipocket.travel.repository.TravelWidgetRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TravelService {

    private final TravelRepository travelRepository;
    private final TravelWidgetRepository widgetRepository;

    /**
     * 여행 폴더 생성
     */
    @Transactional
    public Long createTravel(TravelRequest request) {
        Travel travel = Travel.builder()
                .accountBookId(request.accountBookId())
                .travelPlaceName(request.travelPlaceName())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .imageKey(request.imageKey())
                .build();

        travel.validateDateRange();

        return travelRepository.save(travel).getId();
    }

    /**
     * 가계부별 여행 목록 조회
     */
    public List<TravelResponse> getTravels(Long accountBookId) {
        return travelRepository.findAllByAccountBookId(accountBookId).stream()
                .map(TravelResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 여행 상세 조회 (위젯 포함)
     */
    public TravelDetailResponse getTravelDetail(Long travelId) {
        Travel travel = travelRepository
                .findById(travelId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRAVEL_NOT_FOUND));

        List<WidgetDto> widgets = widgetRepository.findAllByTravelIdOrderByWidgetOrderAsc(travelId).stream()
                .map(WidgetDto::from)
                .collect(Collectors.toList());

        return TravelDetailResponse.of(travel, widgets);
    }

    /**
     * 여행 정보 수정
     */
    @Transactional
    public void updateTravel(Long travelId, TravelRequest request) {
        Travel travel = travelRepository
                .findById(travelId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRAVEL_NOT_FOUND));

        travel.update(
                request.travelPlaceName(),
                request.startDate(),
                request.endDate(),
                request.imageKey());
    }

    /**
     * 여행 삭제
     */
    @Transactional
    public void deleteTravel(Long travelId) {
        Travel travel = travelRepository
                .findById(travelId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRAVEL_NOT_FOUND));

        // Cascade delete widgets? or manual delete?
        // Assuming JPA Cascade or DB Cascade Not set yet.
        // Ideally should delete widgets first.

        widgetRepository.deleteAllByTravelId(travelId);
        travelRepository.delete(travel);
    }

    /**
     * 여행 정보 부분 수정 (Patch)
     */
    @Transactional
    public void patchTravel(Long travelId, com.genesis.unipocket.travel.dto.TravelUpdateRequest request) {
        Travel travel = travelRepository
                .findById(travelId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRAVEL_NOT_FOUND));

        if (request.travelPlaceName() != null) {
            travel.updateName(request.travelPlaceName());
        }
        if (request.imageKey() != null) {
            travel.updateImage(request.imageKey());
        }
        if (request.startDate() != null && request.endDate() != null) {
            travel.updatePeriod(request.startDate(), request.endDate());
        } else if (request.startDate() != null) {
            travel.updatePeriod(request.startDate(), travel.getEndDate());
        } else if (request.endDate() != null) {
            travel.updatePeriod(travel.getStartDate(), request.endDate());
        }
    }

    /**
     * 위젯 배치 수정
     */
    @Transactional
    public void updateWidgets(Long travelId, List<WidgetDto> newWidgets) {
        Travel travel = travelRepository
                .findById(travelId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRAVEL_NOT_FOUND));

        // Simple strategy: Delete all existing and re-create
        // Optimization: Handle diffs if performance issue arises

        List<TravelWidget> currentWidgets = widgetRepository.findAllByTravelIdOrderByWidgetOrderAsc(travelId);

        // 1. Map existing widgets by WidgetType for O(1) lookup
        Map<WidgetType, TravelWidget> currentWidgetMap = currentWidgets.stream()
                .collect(Collectors.toMap(TravelWidget::getWidgetType, widget -> widget));

        List<TravelWidget> widgetsToDelete = new ArrayList<>();

        // 2. Iterate through new widgets to identify updates and additions
        for (WidgetDto dto : newWidgets) {
            if (currentWidgetMap.containsKey(dto.type())) {
                // UPDATE: If exists, update order only
                TravelWidget existingWidget = currentWidgetMap.get(dto.type());
                existingWidget.updateOrder(dto.order());
                currentWidgetMap.remove(dto.type()); // Mark as processed
            } else {
                // INSERT: If not exists, create new
                TravelWidget newWidget = TravelWidget.builder()
                        .travel(travel)
                        .widgetType(dto.type())
                        .widgetOrder(dto.order())
                        .build();
                widgetRepository.save(newWidget);
            }
        }

        // 3. DELETE: Remaining widgets in map are those not present in new request
        widgetsToDelete.addAll(currentWidgetMap.values());
        if (!widgetsToDelete.isEmpty()) {
            widgetRepository.deleteAll(widgetsToDelete);
        }
    }
}
