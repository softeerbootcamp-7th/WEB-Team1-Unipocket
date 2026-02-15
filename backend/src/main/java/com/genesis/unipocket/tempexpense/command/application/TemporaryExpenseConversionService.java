package com.genesis.unipocket.tempexpense.command.application;

import com.genesis.unipocket.expense.command.persistence.entity.ExpenseEntity;
import com.genesis.unipocket.expense.command.persistence.entity.dto.ExpenseManualCreateArgs;
import com.genesis.unipocket.expense.command.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.tempexpense.command.application.result.BatchConversionResult;
import com.genesis.unipocket.tempexpense.command.application.result.ConversionResult;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import java.math.BigDecimal;
import java.time.ZoneOffset;
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

	/**
	 * 단일 변환
	 */
	@Transactional
	public ExpenseEntity convertToExpense(Long accountBookId, Long tempExpenseId) {
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

		// 3. accountBookId 조회 (meta)
		TempExpenseMeta meta =
				metaRepository
						.findById(temp.getTempExpenseMetaId())
						.orElseThrow(() -> new IllegalArgumentException("메타데이터를 찾을 수 없습니다."));
		if (!meta.getAccountBookId().equals(accountBookId)) {
			throw new IllegalArgumentException("가계부와 임시지출내역이 일치하지 않습니다.");
		}

		// 4. ExpenseManualCreateArgs 생성 (manual 팩토리 메서드 사용)
		BigDecimal exchangeRate = temp.getExchangeRate();
		if (exchangeRate == null
				&& temp.getLocalCurrencyAmount() != null
				&& temp.getBaseCurrencyAmount() != null
				&& temp.getLocalCurrencyAmount().compareTo(BigDecimal.ZERO) > 0) {
			exchangeRate =
					temp.getBaseCurrencyAmount()
							.divide(
									temp.getLocalCurrencyAmount(),
									4,
									java.math.RoundingMode.HALF_UP);
		}

		ExpenseManualCreateArgs args =
				new ExpenseManualCreateArgs(
						meta.getAccountBookId(),
						temp.getMerchantName(),
						temp.getCategory(),
						null, // userCardId - 임시지출에서는 카드 정보 없음
						temp.getOccurredAt().atOffset(ZoneOffset.UTC),
						temp.getLocalCurrencyAmount(),
						temp.getLocalCountryCode() != null
								? temp.getLocalCountryCode()
								: com.genesis.unipocket.global.common.enums.CurrencyCode.KRW,
						temp.getBaseCurrencyAmount(),
						temp.getBaseCountryCode() != null
								? temp.getBaseCountryCode()
								: com.genesis.unipocket.global.common.enums.CurrencyCode.KRW,
						null,
						null,
						temp.getMemo(),
						null,
						exchangeRate);

		// 5. Expense 생성 (manual 메서드 사용)
		ExpenseEntity expense = ExpenseEntity.manual(args);

		// 6. Expense 저장
		ExpenseEntity savedExpense = expenseRepository.save(expense);

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
	public BatchConversionResult convertBatch(Long accountBookId, List<Long> tempExpenseIds) {
		if (tempExpenseIds == null || tempExpenseIds.isEmpty()) {
			return new BatchConversionResult(0, 0, 0, List.of());
		}
		log.info("Starting batch conversion for {} temporary expenses", tempExpenseIds.size());

		List<ConversionResult> results = new ArrayList<>();
		int successCount = 0;
		int failedCount = 0;

		for (Long tempExpenseId : tempExpenseIds) {
			try {
				ExpenseEntity expense = convertToExpense(accountBookId, tempExpenseId);
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

	@Transactional
	public BatchConversionResult convertMeta(
			Long accountBookId, Long tempExpenseMetaId, List<Long> tempExpenseIds) {
		TempExpenseMeta meta =
				metaRepository
						.findById(tempExpenseMetaId)
						.orElseThrow(() -> new IllegalArgumentException("메타데이터를 찾을 수 없습니다."));
		if (!meta.getAccountBookId().equals(accountBookId)) {
			throw new IllegalArgumentException("가계부와 메타데이터가 일치하지 않습니다.");
		}

		List<Long> targetIds =
				(tempExpenseIds == null || tempExpenseIds.isEmpty())
						? tempExpenseRepository.findByTempExpenseMetaId(tempExpenseMetaId).stream()
								.map(TemporaryExpense::getTempExpenseId)
								.toList()
						: tempExpenseIds;

		return convertBatch(accountBookId, targetIds);
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
}
