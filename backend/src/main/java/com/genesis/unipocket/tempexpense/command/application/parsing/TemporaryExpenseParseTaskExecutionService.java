package com.genesis.unipocket.tempexpense.command.application.parsing;

import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.tempexpense.command.application.parsing.exception.TemporaryExpenseGeminiRateLimitException;
import com.genesis.unipocket.tempexpense.command.application.parsing.result.AccountBookRateContext;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookRateInfoProvider;
import com.genesis.unipocket.tempexpense.command.facade.port.dto.AccountBookRateInfo;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.common.infrastructure.sse.ParsingProgressPublisher;
import com.genesis.unipocket.tempexpense.common.util.TemporaryExpenseBatchTaskRunner;
import com.genesis.unipocket.tempexpense.common.util.TemporaryExpenseTaskSupport;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class TemporaryExpenseParseTaskExecutionService {

	private final AccountBookRateInfoProvider accountBookRateInfoProvider;
	private final ParsingProgressPublisher progressPublisher;
	private final TemporaryExpenseParseFileService parseFileService;

	TemporaryExpenseParseTaskExecutionService(
			AccountBookRateInfoProvider accountBookRateInfoProvider,
			ParsingProgressPublisher progressPublisher,
			TemporaryExpenseParseFileService parseFileService) {
		this.accountBookRateInfoProvider = accountBookRateInfoProvider;
		this.progressPublisher = progressPublisher;
		this.parseFileService = parseFileService;
	}

	void processParseTaskFiles(TempExpenseMeta meta, List<File> files, String taskId) {
		log.info("Starting async batch parsing for task: {}, files: {}", taskId, files.size());

		AccountBookRateContext rateContext;
		try {
			rateContext = resolveRateContext(meta.getAccountBookId());
		} catch (Exception e) {
			String errorMessage =
					TemporaryExpenseTaskSupport.resolveClientErrorMessage(
							e, ErrorCode.TEMP_EXPENSE_PARSE_FAILED);
			progressPublisher.publishError(taskId, errorMessage);
			throw TemporaryExpenseTaskSupport.rethrow(e);
		}

		TemporaryExpenseBatchTaskRunner.run(
				taskId,
				files,
				progressPublisher,
				ErrorCode.TEMP_EXPENSE_PARSE_FAILED,
				file -> {
					if (!Objects.equals(file.getTempExpenseMetaId(), meta.getTempExpenseMetaId())) {
						log.warn(
								"Skipping out-of-meta file. fileId={}, expectedMetaId={},"
										+ " actualMetaId={}",
								file.getFileId(),
								meta.getTempExpenseMetaId(),
								file.getTempExpenseMetaId());
						return;
					}
					parseFileService.parseFileAndSaveTempExpenses(file, meta, rateContext);
				},
				(file, e) -> {
					if (e instanceof TemporaryExpenseGeminiRateLimitException) {
						log.error("Gemini 429 encountered. aborting parse task. taskId={}", taskId);
						return true;
					}
					log.error("Failed to parse file: {}", file.getS3Key(), e);
					return false;
				});
	}

	private AccountBookRateContext resolveRateContext(Long accountBookId) {
		AccountBookRateInfo accountBook = accountBookRateInfoProvider.getRateInfo(accountBookId);
		return new AccountBookRateContext(
				accountBook.baseCurrencyCode(), accountBook.localCurrencyCode());
	}
}
