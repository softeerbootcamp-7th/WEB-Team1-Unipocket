package com.genesis.unipocket.tempexpense.command.facade;

import com.genesis.unipocket.tempexpense.command.application.FileUploadService;
import com.genesis.unipocket.tempexpense.command.application.TemporaryExpenseBulkUpdateService;
import com.genesis.unipocket.tempexpense.command.application.TemporaryExpenseConversionService;
import com.genesis.unipocket.tempexpense.command.application.TemporaryExpenseRateLimitService;
import com.genesis.unipocket.tempexpense.command.application.parsing.TemporaryExpenseParsingService;
import com.genesis.unipocket.tempexpense.command.application.result.ConfirmStartResult;
import com.genesis.unipocket.tempexpense.command.application.result.FileUploadResult;
import com.genesis.unipocket.tempexpense.command.application.result.ParseStartResult;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookOwnershipValidator;
import com.genesis.unipocket.tempexpense.command.presentation.request.PresignedUrlRequest.UploadType;
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
	private final TemporaryExpenseRateLimitService temporaryExpenseRateLimitService;
	private final AccountBookOwnershipValidator accountBookOwnershipValidator;

	public FileUploadResult createPresignedUrl(
			Long accountBookId,
			String fileName,
			String mimeType,
			UploadType uploadType,
			Long tempExpenseMetaId,
			UUID userId) {
		// 가계부 소유주 검증 진행
		validateOwnership(accountBookId, userId);
		temporaryExpenseRateLimitService.validateUploadRequest(userId);

		// presigned url 발급
		return fileUploadService.createPresignedUrl(
				accountBookId, fileName, mimeType, uploadType, tempExpenseMetaId);
	}

	public ParseStartResult startParseAsync(
			Long accountBookId, Long tempExpenseMetaId, List<String> s3Keys, UUID userId) {
		validateOwnership(accountBookId, userId);
		temporaryExpenseRateLimitService.validateParseRequest(userId);

		// 비동기 파싱(SSE 기반 파싱) 시작
		return temporaryExpenseParsingService.startParseAsync(
				accountBookId, tempExpenseMetaId, s3Keys);
	}

	public ConfirmStartResult confirm(Long accountBookId, Long tempExpenseMetaId, UUID userId) {

		validateOwnership(accountBookId, userId);

		return temporaryExpenseConversionService.startConfirmAsync(accountBookId, tempExpenseMetaId);
	}

	public void deleteMeta(Long accountBookId, Long tempExpenseMetaId, UUID userId) {
		validateOwnership(accountBookId, userId);
		fileUploadService.deleteMeta(accountBookId, tempExpenseMetaId);
	}

	/**
	 * 임시지출내역 리스트 단위 수정 API
	 */
	public TemporaryExpenseMetaBulkUpdateResponse updateTemporaryExpensesByFile(
			Long accountBookId,
			Long tempExpenseMetaId,
			Long fileId,
			TemporaryExpenseMetaBulkUpdateRequest request,
			UUID userId) {

		validateOwnership(accountBookId, userId);

		return TemporaryExpenseMetaBulkUpdateResponse.from(
				temporaryExpenseBulkUpdateService.updateByFile(
						accountBookId, tempExpenseMetaId, fileId, request));
	}

	public void deleteTemporaryExpenseByFile(
			Long accountBookId, Long tempExpenseMetaId, Long fileId, Long tempExpenseId, UUID userId) {
		validateOwnership(accountBookId, userId);
		temporaryExpenseBulkUpdateService.deleteByFile(
				accountBookId, tempExpenseMetaId, fileId, tempExpenseId);
	}

	private void validateOwnership(Long accountBookId, UUID userId) {
		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());
	}
}
