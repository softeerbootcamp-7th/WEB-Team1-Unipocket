package com.genesis.unipocket.tempexpense.command.application.parsing;

import com.genesis.unipocket.tempexpense.command.application.result.ParseStartResult;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookRateInfoProvider;
import com.genesis.unipocket.tempexpense.command.facade.port.ExchangeRateProvider;
import com.genesis.unipocket.tempexpense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.tempexpense.common.infrastructure.sse.ParsingProgressPublisher;
import java.util.List;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class TemporaryExpenseParsingService {

	private final TemporaryExpenseParseTaskStartService parseTaskStartService;

	public TemporaryExpenseParsingService(
			FileRepository fileRepository,
			AccountBookRateInfoProvider accountBookRateInfoProvider,
			ExchangeRateProvider exchangeRateProvider,
			TemporaryExpenseFieldParser fieldParser,
			TemporaryExpenseParseClient temporaryExpenseParseClient,
			TemporaryExpensePersistenceService temporaryExpensePersistenceService,
			ParsingProgressPublisher progressPublisher,
			TempExpenseMetaRepository tempExpenseMetaRepository,
			@Qualifier("parsingExecutor") Executor parsingExecutor) {
		TemporaryExpenseParseFileService parseFileService =
				new TemporaryExpenseParseFileService(
						fieldParser,
						temporaryExpenseParseClient,
						exchangeRateProvider,
						temporaryExpensePersistenceService);
		TemporaryExpenseParseTaskExecutionService parseTaskExecutionService =
				new TemporaryExpenseParseTaskExecutionService(
						accountBookRateInfoProvider, progressPublisher, parseFileService);
		this.parseTaskStartService =
				new TemporaryExpenseParseTaskStartService(
						fileRepository,
						tempExpenseMetaRepository,
						progressPublisher,
						parsingExecutor,
						parseTaskExecutionService);
	}

	public ParseStartResult startParseTask(Long tempExpenseMetaId, List<String> s3Keys) {
		return parseTaskStartService.startParseTask(tempExpenseMetaId, s3Keys);
	}
}
