package com.genesis.unipocket.tempexpense.command.facade.provide;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TempExpenseMetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TemporaryExpenseScopeValidationProvider {

	private final TempExpenseMetaRepository tempExpenseMetaRepository;
	private final FileRepository fileRepository;

	public TempExpenseMeta validateMetaScope(Long accountBookId, Long tempExpenseMetaId) {
		TempExpenseMeta meta =
				tempExpenseMetaRepository
						.findById(tempExpenseMetaId)
						.orElseThrow(
								() -> new BusinessException(ErrorCode.TEMP_EXPENSE_META_NOT_FOUND));
		if (!accountBookId.equals(meta.getAccountBookId())) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_SCOPE_MISMATCH);
		}
		return meta;
	}

	public File validateFileScope(Long accountBookId, Long tempExpenseMetaId, Long fileId) {
		validateMetaScope(accountBookId, tempExpenseMetaId);
		return validateFileInMeta(tempExpenseMetaId, fileId);
	}

	public File validateFileInMeta(Long tempExpenseMetaId, Long fileId) {
		File file =
				fileRepository
						.findById(fileId)
						.orElseThrow(
								() -> new BusinessException(ErrorCode.TEMP_EXPENSE_FILE_NOT_FOUND));
		if (!tempExpenseMetaId.equals(file.getTempExpenseMetaId())) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_SCOPE_MISMATCH);
		}
		return file;
	}

	public BusinessException resolveScopeException(
			Long accountBookId, Long tempExpenseMetaId, Long fileId) {
		try {
			validateMetaScope(accountBookId, tempExpenseMetaId);
		} catch (BusinessException e) {
			return e;
		}

		try {
			validateFileInMeta(tempExpenseMetaId, fileId);
		} catch (BusinessException e) {
			return e;
		}

		return new BusinessException(ErrorCode.TEMP_EXPENSE_SCOPE_MISMATCH);
	}
}
