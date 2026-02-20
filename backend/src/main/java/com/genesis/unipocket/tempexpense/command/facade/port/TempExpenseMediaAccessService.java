package com.genesis.unipocket.tempexpense.command.facade.port;

import com.genesis.unipocket.media.command.application.result.PresignedUrlResult;
import java.time.Duration;

public interface TempExpenseMediaAccessService {
	PresignedUrlResult issueUploadPath(Long accountBookId, String mimeType);

	String issueGetPath(String mediaKey, Duration expiration);

	byte[] download(String mediaKey);
}
