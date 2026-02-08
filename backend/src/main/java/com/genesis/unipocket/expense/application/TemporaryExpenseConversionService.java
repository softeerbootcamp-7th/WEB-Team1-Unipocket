package com.genesis.unipocket.expense.application;

import com.genesis.unipocket.expense.persistence.dto.ExpenseManualCreateArgs;
import com.genesis.unipocket.expense.persistence.entity.expense.*;
import com.genesis.unipocket.expense.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.expense.persistence.repository.FileRepository;
import com.genesis.unipocket.expense.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.expense.persistence.repository.TemporaryExpenseRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>임시지출내역 변환 서비스</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
@Slf4j
@Service
@AllArgsConstructor
public class TemporaryExpenseConversionService {

	private final TemporaryExpenseRepository tempExpenseRepository;
	private final ExpenseRepository expenseRepository;
	private final TempExpenseMetaRepository metaRepository;
	private final FileRepository fileRepository;

	/**
	 * 단일 변환
	 */
	@Transactional
	public Expense convertToExpense(Long tempExpenseId) {
		log.info("Converting temporary expense to permanent: {}", tempExpenseId);

		// 1. TemporaryExpense 조회
		TemporaryExpense temp =
				tempExpenseRepository
						.findById(tempExpenseId)
						.orElseThrow(
								() ->
										new IllegalArgumentException(
												"임시지출내역을 찾을 수 없습니다. ID: " + tempExpenseId));

		// 2. 필수 필드 검증
		validateRequiredFields(temp);

		// 3. accountBookId 조회 (fileId → meta)
		File file =
				fileRepository
						.findById(temp.getFileId())
						.orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));
		TempExpenseMeta meta =
				metaRepository
						.findById(file.getTempExpenseMetaId())
						.orElseThrow(() -> new IllegalArgumentException("메타데이터를 찾을 수 없습니다."));

		// 4. ExpenseManualCreateArgs 생성 (manual 팩토리 메서드 사용)
		ExpenseManualCreateArgs args =
				new ExpenseManualCreateArgs(
						meta.getAccountBookId(),
						temp.getMerchantName(),
						temp.getCategory(),
						temp.getPaymentsMethod(),
						temp.getOccurredAt(),
						temp.getLocalCurrencyAmount(),
						temp.getLocalCountryCode() != null
								? temp.getLocalCountryCode()
								: com.genesis.unipocket.global.common.enums.CurrencyCode.KRW,
						temp.getBaseCurrencyAmount(),
						temp.getBaseCountryCode() != null
								? temp.getBaseCountryCode()
								: com.genesis.unipocket.global.common.enums.CurrencyCode.KRW,
						temp.getMemo());

		// 5. Expense 생성 (manual 메서드 사용)
		Expense expense = Expense.manual(args);

		// 6. Expense 저장
		Expense savedExpense = expenseRepository.save(expense);

		// 7. TemporaryExpense 삭제
		tempExpenseRepository.delete(temp);

		log.info(
				"Converted temporary expense {} to expense {}",
				tempExpenseId,
				savedExpense.getExpenseId());

		return savedExpense;
	}

	/**
	 * Batch 변환
	 */
	@Transactional
	public BatchConversionResult convertBatch(List<Long> tempExpenseIds) {
		log.info("Starting batch conversion for {} temporary expenses", tempExpenseIds.size());

		List<ConversionResult> results = new ArrayList<>();
		int successCount = 0;
		int failedCount = 0;

		for (Long tempExpenseId : tempExpenseIds) {
			try {
				Expense expense = convertToExpense(tempExpenseId);
				results.add(
						new ConversionResult(
								tempExpenseId, expense.getExpenseId(), "SUCCESS", null));
				successCount++;
			} catch (Exception e) {
				log.error("Failed to convert temporary expense: {}", tempExpenseId, e);
				results.add(new ConversionResult(tempExpenseId, null, "FAILED", e.getMessage()));
				failedCount++;
			}
		}

		log.info(
				"Batch conversion completed: {} total, {} success, {} failed",
				tempExpenseIds.size(),
				successCount,
				failedCount);

		return new BatchConversionResult(tempExpenseIds.size(), successCount, failedCount, results);
	}

	/**
	 * 필수 필드 검증
	 */
	private void validateRequiredFields(TemporaryExpense temp) {
		if (temp.getMerchantName() == null || temp.getMerchantName().isBlank()) {
			throw new IllegalArgumentException("가맹점명은 필수입니다.");
		}
		if (temp.getLocalCurrencyAmount() == null) {
			throw new IllegalArgumentException("금액은 필수입니다.");
		}
		if (temp.getOccurredAt() == null) {
			throw new IllegalArgumentException("거래일시는 필수입니다.");
		}
		// Category는 null 허용 (UNCLASSIFIED로 변환 가능)
	}

	/**
	 * Batch 변환 결과
	 */
	public record BatchConversionResult(
			int totalRequested,
			int successCount,
			int failedCount,
			List<ConversionResult> results) {}

	/**
	 * 개별 변환 결과
	 */
	public record ConversionResult(
			Long tempExpenseId, Long expenseId, String status, String reason) {}
}
