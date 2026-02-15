package com.genesis.unipocket.travel.command.facade;

import com.genesis.unipocket.travel.command.application.TravelCommandService;
import com.genesis.unipocket.travel.command.application.command.CreateTravelCommand;
import com.genesis.unipocket.travel.command.application.command.PatchTravelCommand;
import com.genesis.unipocket.travel.command.application.command.UpdateTravelCommand;
import com.genesis.unipocket.travel.command.application.command.UpdateWidgetsCommand;
import com.genesis.unipocket.travel.command.application.result.CreateTravelResult;
import com.genesis.unipocket.travel.command.facade.port.TravelDefaultWidgetPort;
import com.genesis.unipocket.travel.command.presentation.request.TravelRequest;
import com.genesis.unipocket.travel.command.presentation.request.TravelUpdateRequest;
import com.genesis.unipocket.travel.command.presentation.request.WidgetRequest;
import com.genesis.unipocket.travel.common.validate.UserAccountBookValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TravelCommandFacade {

	private final TravelCommandService travelCommandService;
	private final UserAccountBookValidator userAccountBookValidator;
	private final TravelDefaultWidgetPort travelDefaultWidgetPort;

	public Long createTravel(TravelRequest request, UUID userId) {
		userAccountBookValidator.validateUserAccountBook(
				userId.toString(), request.accountBookId());
		CreateTravelCommand command = CreateTravelCommand.from(request);
		CreateTravelResult result = travelCommandService.createTravel(command);

		Long travelId = result.travelId();
		travelDefaultWidgetPort.setDefaultWidget(travelId);
		return travelId;
	}

	public void updateTravel(Long travelId, TravelRequest request, UUID userId) {
		userAccountBookValidator.validateUserAccountBook(
				userId.toString(), request.accountBookId());
		UpdateTravelCommand command = UpdateTravelCommand.of(travelId, request);
		travelCommandService.updateTravel(command);
	}

	public void patchTravel(Long travelId, TravelUpdateRequest request, UUID userId) {
		userAccountBookValidator.validateUserAccountBook(userId.toString(), travelId);
		PatchTravelCommand command = PatchTravelCommand.of(travelId, request);
		travelCommandService.patchTravel(command);
	}

	public void deleteTravel(Long travelId, UUID userId) {
		userAccountBookValidator.validateUserAccountBook(userId.toString(), travelId);
		travelCommandService.deleteTravel(travelId);
	}

	public void updateWidgets(Long travelId, List<WidgetRequest> widgets, UUID userId) {
		userAccountBookValidator.validateUserAccountBook(userId.toString(), travelId);
		UpdateWidgetsCommand command = UpdateWidgetsCommand.of(travelId, widgets);
		travelCommandService.updateWidgets(command);
	}
}
