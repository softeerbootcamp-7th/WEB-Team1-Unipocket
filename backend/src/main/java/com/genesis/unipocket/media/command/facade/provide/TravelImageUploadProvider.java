package com.genesis.unipocket.media.command.facade.provide;

import com.genesis.unipocket.global.infrastructure.MediaContentType;
import com.genesis.unipocket.media.command.application.MediaObjectStorage;
import com.genesis.unipocket.media.command.application.result.PresignedUrlResult;
import com.genesis.unipocket.travel.command.facade.port.TravelImageUploadPathIssueService;
import com.genesis.unipocket.travel.command.facade.port.dto.TravelImageUploadPathInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * <b>여행 폴더 썸네일 이미지 업로드 구현체</b>
 * @author bluefishez
 * @since 2026-02-11
 */
@Component
@RequiredArgsConstructor
public class TravelImageUploadProvider implements TravelImageUploadPathIssueService {

	private final MediaObjectStorage mediaObjectStorage;

	@Override
	public TravelImageUploadPathInfo issueTravelImageUploadPath(
			Long accountBookId, MediaContentType mediaContentType) {

		// TODO: Travel 도메인 내에서 accountBook 을 지원해주는지 확인하는 포트 및 구현체 필요

		PresignedUrlResult response =
				mediaObjectStorage.getPresignedUrl("travels", "upload" + mediaContentType.getExt());

		return new TravelImageUploadPathInfo(response.presignedUrl(), response.imageKey());
	}
}
