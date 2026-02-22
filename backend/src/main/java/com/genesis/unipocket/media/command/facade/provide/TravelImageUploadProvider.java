package com.genesis.unipocket.media.command.facade.provide;

import com.genesis.unipocket.global.infrastructure.MediaContentType;
import com.genesis.unipocket.media.command.application.MediaObjectStorage;
import com.genesis.unipocket.media.command.application.MediaPathPrefixManager;
import com.genesis.unipocket.media.command.application.result.PresignedUrlResult;
import com.genesis.unipocket.travel.command.facade.port.TravelImageUploadPathIssueService;
import com.genesis.unipocket.travel.command.facade.port.dto.TravelImageUploadPathInfo;
import com.genesis.unipocket.travel.query.port.TravelImageAccessService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * <b>여행 폴더 썸네일 이미지 업로드 구현체</b>
 * @author bluefishez
 * @since 2026-02-11
 */
@Component
@RequiredArgsConstructor
public class TravelImageUploadProvider
		implements TravelImageUploadPathIssueService, TravelImageAccessService {

	private final MediaObjectStorage mediaObjectStorage;
	private final MediaPathPrefixManager mediaPathPrefixManager;

	@Override
	public TravelImageUploadPathInfo issueTravelImageUploadPath(
			Long accountBookId, MediaContentType mediaContentType) {

		// TODO: Travel 도메인 내에서 accountBook 을 지원해주는지 확인하는 포트 및 구현체 필요

		PresignedUrlResult response =
				mediaObjectStorage.getPresignedUrl(
						mediaPathPrefixManager.getTravelImagePrefix(), mediaContentType);

		return new TravelImageUploadPathInfo(response.presignedUrl(), response.imageKey());
	}

	@Override
	public boolean isTravelImageKey(String imageKey) {
		return mediaPathPrefixManager.isTravelImageKey(imageKey);
	}

	@Override
	public boolean exists(String imageKey) {
		return mediaObjectStorage.exists(imageKey);
	}

	@Override
	public String issueGetPath(String imageKey, Duration expiration) {
		return mediaObjectStorage.getPresignedGetUrl(imageKey, expiration);
	}
}
