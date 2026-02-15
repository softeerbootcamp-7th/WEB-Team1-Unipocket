package com.genesis.unipocket.media.command.facade.provide;

import com.genesis.unipocket.media.command.application.MediaObjectStorage;
import com.genesis.unipocket.media.command.application.result.PresignedUrlResult;
import com.genesis.unipocket.tempexpense.command.facade.port.TempExpenseMediaAccessService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * <b>TempExpense 미디어 경로 발급/조회 Provider</b>
 */
@Component
@RequiredArgsConstructor
public class TempExpenseMediaProvider implements TempExpenseMediaAccessService {

	private static final String TEMP_EXPENSE_PREFIX = "temp-expenses";
	private final MediaObjectStorage mediaObjectStorage;

	@Override
	public PresignedUrlResult issueUploadPath(Long accountBookId, String originalFileName) {
		String prefix = TEMP_EXPENSE_PREFIX + "/" + accountBookId;
		return mediaObjectStorage.getPresignedUrl(prefix, originalFileName);
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
