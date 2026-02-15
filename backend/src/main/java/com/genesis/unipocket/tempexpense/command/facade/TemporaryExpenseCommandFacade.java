package com.genesis.unipocket.tempexpense.command.facade;

import com.genesis.unipocket.tempexpense.command.application.FileUploadService;
import com.genesis.unipocket.tempexpense.command.application.TemporaryExpenseBulkUpdateService;
import com.genesis.unipocket.tempexpense.command.application.TemporaryExpenseConversionService;
import com.genesis.unipocket.tempexpense.command.application.parsing.TemporaryExpenseParsingService;
import com.genesis.unipocket.tempexpense.command.application.result.BatchConversionResult;
import com.genesis.unipocket.tempexpense.command.application.result.FileUploadResult;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookOwnershipValidator;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File.FileType;
import com.genesis.unipocket.tempexpense.command.presentation.request.TemporaryExpenseMetaBulkUpdateRequest;
import com.genesis.unipocket.tempexpense.command.presentation.response.TemporaryExpenseMetaBulkUpdateResponse;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <b>임시지출내역 Facade 클래스</b>
 * <p>
 * 임시지출내역 도메인에 대한 요청 처리
 *
 * @author 김동균
 * @since 2026-02-08
 */
@Service
@AllArgsConstructor
public class TemporaryExpenseCommandFacade {

	private final FileUploadService fileUploadService;
	private final TemporaryExpenseParsingService temporaryExpenseParsingService;
	private final TemporaryExpenseConversionService temporaryExpenseConversionService;
	private final TemporaryExpenseBulkUpdateService temporaryExpenseBulkUpdateService;
	private final AccountBookOwnershipValidator accountBookOwnershipValidator;

	public FileUploadResult createPresignedUrl(
			Long accountBookId, String fileName, FileType fileType, UUID userId) {
		validateOwnership(accountBookId, userId);
		return fileUploadService.createPresignedUrl(accountBookId, fileName, fileType);
	}

	public String startParseAsync(Long accountBookId, List<String> s3Keys, UUID userId) {
		validateOwnership(accountBookId, userId);
		return temporaryExpenseParsingService.startParseAsync(accountBookId, s3Keys);
	}

	public BatchConversionResult confirm(
			Long accountBookId, Long tempExpenseMetaId, List<Long> tempExpenseIds, UUID userId) {
		validateOwnership(accountBookId, userId);
		return temporaryExpenseConversionService.convertMeta(
				accountBookId, tempExpenseMetaId, tempExpenseIds);
	}

	public void deleteMeta(Long accountBookId, Long tempExpenseMetaId, UUID userId) {
		validateOwnership(accountBookId, userId);
		fileUploadService.deleteMeta(accountBookId, tempExpenseMetaId);
	}

	public TemporaryExpenseMetaBulkUpdateResponse updateTemporaryExpensesByMeta(
			Long accountBookId,
			Long tempExpenseMetaId,
			TemporaryExpenseMetaBulkUpdateRequest request,
			UUID userId) {
		validateOwnership(accountBookId, userId);
		return TemporaryExpenseMetaBulkUpdateResponse.from(
				temporaryExpenseBulkUpdateService.updateByMeta(
						accountBookId, tempExpenseMetaId, request));
	}

	private void validateOwnership(Long accountBookId, UUID userId) {
		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());
	}
}
