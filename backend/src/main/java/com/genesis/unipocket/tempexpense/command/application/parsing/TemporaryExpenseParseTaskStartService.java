package com.genesis.unipocket.tempexpense.command.application.parsing;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.tempexpense.command.application.result.ParseStartResult;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.tempexpense.common.infrastructure.sse.ParsingProgressPublisher;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

final class TemporaryExpenseParseTaskStartService {

	private static final int MAX_DOCUMENT_PARSE_FILES = 1;
	private static final int MAX_IMAGE_PARSE_FILES = 10;

	private final FileRepository fileRepository;
	private final TempExpenseMetaRepository tempExpenseMetaRepository;
	private final ParsingProgressPublisher progressPublisher;
	private final Executor parsingExecutor;
	private final TemporaryExpenseParseTaskExecutionService parseTaskExecutionService;

	TemporaryExpenseParseTaskStartService(
			FileRepository fileRepository,
			TempExpenseMetaRepository tempExpenseMetaRepository,
			ParsingProgressPublisher progressPublisher,
			Executor parsingExecutor,
			TemporaryExpenseParseTaskExecutionService parseTaskExecutionService) {
		this.fileRepository = fileRepository;
		this.tempExpenseMetaRepository = tempExpenseMetaRepository;
		this.progressPublisher = progressPublisher;
		this.parsingExecutor = parsingExecutor;
		this.parseTaskExecutionService = parseTaskExecutionService;
	}

	ParseStartResult startParseTask(Long tempExpenseMetaId, List<String> s3Keys) {
		List<String> requestedS3Keys = s3Keys == null ? List.of() : s3Keys;

		if (tempExpenseMetaId == null) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_META_NOT_FOUND);
		}

		TempExpenseMeta meta =
				tempExpenseMetaRepository
						.findById(tempExpenseMetaId)
						.orElseThrow(
								() -> new BusinessException(ErrorCode.TEMP_EXPENSE_META_NOT_FOUND));

		Long accountBookId = meta.getAccountBookId();

		List<File> metaFiles = fileRepository.findByTempExpenseMetaId(tempExpenseMetaId);
		if (metaFiles.isEmpty()) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_PARSE_FILES_REQUIRED);
		}

		List<File> files = resolveRequestedFiles(metaFiles, requestedS3Keys);
		validateParseFileLimit(files);

		String taskId = UUID.randomUUID().toString();
		progressPublisher.registerTask(taskId, accountBookId);
		parsingExecutor.execute(
				() -> parseTaskExecutionService.processParseTaskFiles(meta, files, taskId));
		return new ParseStartResult(taskId, files.size());
	}

	private List<File> resolveRequestedFiles(List<File> metaFiles, List<String> requestedS3Keys) {
		if (requestedS3Keys.isEmpty()) {
			return metaFiles;
		}

		Map<String, File> metaFileByKey =
				metaFiles.stream().collect(Collectors.toMap(File::getS3Key, Function.identity()));
		Set<String> distinctKeys = new HashSet<>(requestedS3Keys);
		List<File> selectedFiles =
				distinctKeys.stream().map(metaFileByKey::get).filter(Objects::nonNull).toList();
		if (selectedFiles.size() != distinctKeys.size()) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_FILE_NOT_FOUND);
		}
		return selectedFiles;
	}

	private void validateParseFileLimit(List<File> files) {
		boolean hasDocument =
				files.stream()
						.anyMatch(
								f ->
										f.getFileType() == File.FileType.CSV
												|| f.getFileType() == File.FileType.EXCEL);
		if (hasDocument && files.size() > MAX_DOCUMENT_PARSE_FILES) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_PARSE_FILE_LIMIT_EXCEEDED);
		}
		if (!hasDocument && files.size() > MAX_IMAGE_PARSE_FILES) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_PARSE_FILE_LIMIT_EXCEEDED);
		}
	}
}
