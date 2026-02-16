package com.genesis.unipocket.tempexpense.command.application.parsing;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.infrastructure.MediaContentType;
import com.genesis.unipocket.global.infrastructure.gemini.GeminiService;
import com.genesis.unipocket.tempexpense.command.facade.port.TempExpenseMediaAccessService;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import java.time.Duration;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TemporaryExpenseParseClient {

	private final GeminiService geminiService;
	private final TempExpenseMediaAccessService tempExpenseMediaAccessService;
	private final TemporaryExpenseContentExtractor contentExtractor;

	public GeminiService.GeminiParseResponse parse(File file) {
		if (file.getFileType() == File.FileType.IMAGE) {
			String s3Url =
					tempExpenseMediaAccessService.issueGetPath(
							file.getS3Key(), Duration.ofMinutes(10));
			String mimeType = resolveMimeType(file.getS3Key());
			return geminiService.parseReceiptImage(s3Url, mimeType);
		}
		String content = contentExtractor.extractContent(file);
		return geminiService.parseDocument(content);
	}

	private String resolveMimeType(String s3Key) {
		int idx = s3Key.lastIndexOf('.');
		if (idx < 0 || idx == s3Key.length() - 1) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_INVALID_FILE_TYPE);
		}

		String extension = s3Key.substring(idx).toLowerCase(Locale.ROOT);
		return MediaContentType.fromExtension(extension)
				.map(MediaContentType::getMimeType)
				.orElseThrow(() -> new BusinessException(ErrorCode.TEMP_EXPENSE_INVALID_FILE_TYPE));
	}
}
