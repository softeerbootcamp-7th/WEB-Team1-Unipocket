package com.genesis.unipocket.travel.command.facade.port;

import com.genesis.unipocket.global.infrastructure.MediaContentType;
import com.genesis.unipocket.travel.command.facade.port.dto.TravelImageUploadPathInfo;

public interface TravelImageUploadPathIssueService {

	TravelImageUploadPathInfo issueTravelImageUploadPath(
			Long accountBookId, MediaContentType mediaContentType);
}
