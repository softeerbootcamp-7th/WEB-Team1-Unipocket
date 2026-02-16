package com.genesis.unipocket.tempexpense.command.facade.port;

import com.genesis.unipocket.media.command.application.result.PresignedUrlResult;
import java.time.Duration;

/**
 * <b>TempExpense 미디어 접근 포트</b>
 */
public interface TempExpenseMediaAccessService {
	PresignedUrlResult issueUploadPath(Long accountBookId, String originalFileName);

	String issueGetPath(String mediaKey, Duration expiration);

	byte[] download(String mediaKey);
}
