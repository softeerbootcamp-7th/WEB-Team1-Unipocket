package com.genesis.unipocket.media.command.facade.provide;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.infrastructure.MediaContentType;
import com.genesis.unipocket.media.command.application.MediaObjectStorage;
import com.genesis.unipocket.media.command.application.MediaPathPrefixManager;
import com.genesis.unipocket.media.command.application.result.PresignedUrlResult;
import com.genesis.unipocket.tempexpense.query.application.port.TempExpenseMediaAccessService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * <b>TempExpense 미디어 경로 발급/조회 Provider</b>
 */
@Component
@RequiredArgsConstructor
public class TempExpenseMediaProvider
		implements com.genesis.unipocket.tempexpense.command.facade.port
						.TempExpenseMediaAccessService,
				TempExpenseMediaAccessService {

	private final MediaObjectStorage mediaObjectStorage;
	private final MediaPathPrefixManager mediaPathPrefixManager;

	@Override
	public PresignedUrlResult issueUploadPath(Long accountBookId, String mimeType) {
		String prefix = mediaPathPrefixManager.getTempExpensePrefix(accountBookId);
		MediaContentType contentType =
				MediaContentType.fromMimeType(mimeType)
						.orElseThrow(() -> new BusinessException(ErrorCode.UNSUPPORTED_MEDIA_TYPE));
		return mediaObjectStorage.getPresignedUrl(prefix, contentType);
	}

	@Override
	public String issueGetPath(String mediaKey, Duration expiration) {
		return mediaObjectStorage.getPresignedGetUrl(mediaKey, expiration);
	}

	@Override
	public byte[] download(String mediaKey) {
		return mediaObjectStorage.download(mediaKey);
	}
}
