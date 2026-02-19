package com.genesis.unipocket.travel.command.facade;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.infrastructure.MediaContentType;
import com.genesis.unipocket.travel.command.application.TravelCommandService;
import com.genesis.unipocket.travel.command.application.command.CreateTravelCommand;
import com.genesis.unipocket.travel.command.application.command.PatchTravelCommand;
import com.genesis.unipocket.travel.command.application.command.UpdateTravelCommand;
import com.genesis.unipocket.travel.command.application.result.CreateTravelResult;
import com.genesis.unipocket.travel.command.facade.port.TravelDefaultWidgetPort;
import com.genesis.unipocket.travel.command.facade.port.TravelImageUploadPathIssueService;
import com.genesis.unipocket.travel.command.facade.port.dto.TravelImageUploadPathInfo;
import com.genesis.unipocket.travel.common.validate.UserAccountBookValidator;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TravelCommandFacade {

	private final TravelCommandService travelCommandService;
	private final UserAccountBookValidator userAccountBookValidator;
	private final TravelDefaultWidgetPort travelDefaultWidgetPort;
	private final TravelImageUploadPathIssueService travelImageUploadPathIssueService;

	@Transactional
	public Long createTravel(CreateTravelCommand command, UUID userId) {
		userAccountBookValidator.validateUserAccountBook(userId.toString(), command.accountBookId());
		CreateTravelResult result = travelCommandService.createTravel(command);

		Long travelId = result.travelId();
		travelDefaultWidgetPort.setDefaultWidget(travelId);
		return travelId;
	}

	@Transactional
	public void updateTravel(Long accountBookId, UpdateTravelCommand command, UUID userId) {
		userAccountBookValidator.validateUserAccountBook(userId.toString(), accountBookId);
		travelCommandService.updateTravel(accountBookId, command);
	}

	@Transactional
	public void patchTravel(PatchTravelCommand command, UUID userId) {
		validateTravelOwnership(userId, command.travelId());
		travelCommandService.patchTravel(command);
	}

	@Transactional
	public void deleteTravel(Long travelId, UUID userId) {
		validateTravelOwnership(userId, travelId);
		travelCommandService.deleteTravel(travelId);
	}

	private void validateTravelOwnership(UUID userId, Long travelId) {
		Long accountBookId = travelCommandService.getTravel(travelId).getAccountBookId();
		userAccountBookValidator.validateUserAccountBook(userId.toString(), accountBookId);
	}

	public TravelImageUploadPathInfo issueTravelImageUploadPath(
			Long accountBookId, UUID userId, String mimeType) {
		userAccountBookValidator.validateUserAccountBook(userId.toString(), accountBookId);
		MediaContentType mediaContentType =
				MediaContentType.fromMimeType(mimeType)
						.orElseThrow(() -> new BusinessException(ErrorCode.UNSUPPORTED_MEDIA_TYPE));
		if (!mediaContentType.getMimeType().startsWith("image/")) {
			throw new BusinessException(ErrorCode.UNSUPPORTED_MEDIA_TYPE);
		}
		return travelImageUploadPathIssueService.issueTravelImageUploadPath(
				accountBookId, mediaContentType);
	}
}
