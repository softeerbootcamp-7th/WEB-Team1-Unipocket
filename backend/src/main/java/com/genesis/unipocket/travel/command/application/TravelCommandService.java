package com.genesis.unipocket.travel.command.application;

import com.genesis.unipocket.global.common.enums.WidgetType;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.travel.command.application.command.CreateTravelCommand;
import com.genesis.unipocket.travel.command.application.command.PatchTravelCommand;
import com.genesis.unipocket.travel.command.application.command.UpdateTravelCommand;
import com.genesis.unipocket.travel.command.application.command.UpdateWidgetsCommand;
import com.genesis.unipocket.travel.command.application.result.CreateTravelResult;
import com.genesis.unipocket.travel.command.persistence.entity.Travel;
import com.genesis.unipocket.travel.command.persistence.entity.TravelWidget;
import com.genesis.unipocket.travel.command.persistence.repository.TravelCommandRepository;
import com.genesis.unipocket.travel.command.persistence.repository.TravelWidgetCommandRepository;
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
public class TravelCommandService {

	private final TravelCommandRepository travelRepository;
	private final TravelWidgetCommandRepository widgetRepository;

	@Transactional
	public CreateTravelResult createTravel(CreateTravelCommand command) {
		Travel travel =
				Travel.builder()
						.accountBookId(command.accountBookId())
						.travelPlaceName(command.travelPlaceName())
						.startDate(command.startDate())
						.endDate(command.endDate())
						.imageKey(command.imageKey())
						.build();

		travel.validateDateRange();

		Long travelId = travelRepository.save(travel).getId();
		return new CreateTravelResult(travelId);
	}

	public Travel getTravel(Long travelId) {
		return travelRepository
				.findById(travelId)
				.orElseThrow(() -> new BusinessException(ErrorCode.TRAVEL_NOT_FOUND));
	}

	@Transactional
	public void updateTravel(UpdateTravelCommand command) {
		Travel travel =
				travelRepository
						.findById(command.travelId())
						.orElseThrow(() -> new BusinessException(ErrorCode.TRAVEL_NOT_FOUND));

		travel.update(
				command.travelPlaceName(),
				command.startDate(),
				command.endDate(),
				command.imageKey());
	}

	@Transactional
	public void deleteTravel(Long travelId) {
		Travel travel =
				travelRepository
						.findById(travelId)
						.orElseThrow(() -> new BusinessException(ErrorCode.TRAVEL_NOT_FOUND));

		widgetRepository.deleteAllByTravelId(travelId);
		travelRepository.delete(travel);
	}

	@Transactional
	public void patchTravel(PatchTravelCommand command) {
		Travel travel =
				travelRepository
						.findById(command.travelId())
						.orElseThrow(() -> new BusinessException(ErrorCode.TRAVEL_NOT_FOUND));

		if (command.travelPlaceName() != null) {
			travel.updateName(command.travelPlaceName());
		}
		if (command.imageKey() != null) {
			travel.updateImage(command.imageKey());
		}
		if (command.startDate() != null && command.endDate() != null) {
			travel.updatePeriod(command.startDate(), command.endDate());
		} else if (command.startDate() != null) {
			travel.updatePeriod(command.startDate(), travel.getEndDate());
		} else if (command.endDate() != null) {
			travel.updatePeriod(travel.getStartDate(), command.endDate());
		}
	}

	@Transactional
	public void updateWidgets(UpdateWidgetsCommand command) {
		Travel travel =
				travelRepository
						.findById(command.travelId())
						.orElseThrow(() -> new BusinessException(ErrorCode.TRAVEL_NOT_FOUND));

		List<TravelWidget> currentWidgets =
				widgetRepository.findAllByTravelIdOrderByWidgetOrderAsc(command.travelId());

		Map<WidgetType, TravelWidget> currentWidgetMap =
				currentWidgets.stream()
						.collect(Collectors.toMap(TravelWidget::getWidgetType, widget -> widget));

		List<TravelWidget> widgetsToDelete = new ArrayList<>();

		for (UpdateWidgetsCommand.Widget widget : command.widgets()) {
			if (currentWidgetMap.containsKey(widget.type())) {
				TravelWidget existingWidget = currentWidgetMap.get(widget.type());
				existingWidget.updateOrder(widget.order());
				currentWidgetMap.remove(widget.type());
			} else {
				TravelWidget newWidget =
						TravelWidget.builder()
								.travel(travel)
								.widgetType(widget.type())
								.widgetOrder(widget.order())
								.build();
				widgetRepository.save(newWidget);
			}
		}

		widgetsToDelete.addAll(currentWidgetMap.values());
		if (!widgetsToDelete.isEmpty()) {
			widgetRepository.deleteAll(widgetsToDelete);
		}
	}
}
