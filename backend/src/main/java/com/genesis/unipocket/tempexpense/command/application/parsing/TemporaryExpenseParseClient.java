package com.genesis.unipocket.tempexpense.command.application.parsing;

import com.genesis.unipocket.global.infrastructure.gemini.GeminiService;
import com.genesis.unipocket.tempexpense.command.facade.port.TempExpenseMediaAccessService;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import java.time.Duration;
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
			return geminiService.parseReceiptImage(s3Url);
		}
		String content = contentExtractor.extractContent(file);
		return geminiService.parseDocument(content);
	}
}
