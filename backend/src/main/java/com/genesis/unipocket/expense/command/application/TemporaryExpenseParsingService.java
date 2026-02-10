package com.genesis.unipocket.expense.command.application;

import com.genesis.unipocket.expense.command.persistence.entity.expense.File;
import com.genesis.unipocket.expense.command.persistence.entity.expense.TempExpenseMeta;
import com.genesis.unipocket.expense.command.persistence.entity.expense.TemporaryExpense;
import com.genesis.unipocket.expense.command.persistence.entity.expense.TemporaryExpense.TemporaryExpenseStatus;
import com.genesis.unipocket.expense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.expense.command.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.expense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.expense.common.enums.Category;
import com.genesis.unipocket.expense.common.infrastructure.ParsingProgressPublisher;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.infrastructure.gemini.GeminiService;
import com.genesis.unipocket.global.infrastructure.gemini.GeminiService.GeminiParseResponse;
import com.genesis.unipocket.global.infrastructure.gemini.GeminiService.ParsedExpenseItem;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>임시지출내역 파싱 서비스</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
@Slf4j
@Service
@AllArgsConstructor
public class TemporaryExpenseParsingService {

	private final FileRepository fileRepository;
	private final TempExpenseMetaRepository tempExpenseMetaRepository;
	private final TemporaryExpenseRepository temporaryExpenseRepository;
	private final GeminiService geminiService;
	private final com.genesis.unipocket.global.infrastructure.aws.S3Service s3Service;
	private final ParsingProgressPublisher progressPublisher;
	private final CsvParsingService csvParsingService;

	/**
	 * 파일 파싱 및 TemporaryExpense 생성
	 */
	@Transactional
	public ParsingResult parseFile(Long fileId) {
		// 1. File 조회
		File file =
				fileRepository
						.findById(fileId)
						.orElseThrow(
								() -> new IllegalArgumentException("파일을 찾을 수 없습니다. ID: " + fileId));

		// 2. TempExpenseMeta 조회
		TempExpenseMeta meta =
				tempExpenseMetaRepository
						.findById(file.getTempExpenseMetaId())
						.orElseThrow(() -> new IllegalArgumentException("메타데이터를 찾을 수 없습니다."));

		// 3. Gemini 파싱 (S3 GET presigned URL 생성)
		String s3Url =
				s3Service.getPresignedGetUrl(file.getS3Key(), java.time.Duration.ofMinutes(10));
		GeminiParseResponse geminiResponse = geminiService.parseReceiptImage(s3Url);

		if (!geminiResponse.success()) {
			throw new RuntimeException("파싱 실패: " + geminiResponse.errorMessage());
		}

		// 4. TemporaryExpense 엔티티 생성
		List<TemporaryExpense> createdExpenses = new ArrayList<>();
		int normalCount = 0;
		int incompleteCount = 0;

		for (ParsedExpenseItem item : geminiResponse.items()) {
			TemporaryExpenseStatus status = determineStatus(item);
			if (status == TemporaryExpenseStatus.NORMAL) normalCount++;
			else if (status == TemporaryExpenseStatus.INCOMPLETE) incompleteCount++;

			TemporaryExpense expense =
					TemporaryExpense.builder()
							.fileId(fileId)
							.merchantName(item.merchantName())
							.category(parseCategory(item.category()))
							.localCountryCode(parseCurrencyCode(item.localCurrency()))
							.localCurrencyAmount(item.localAmount())
							.baseCountryCode(
									parseCurrencyCode(item.localCurrency())) // same as local
							.baseCurrencyAmount(item.localAmount()) // same as local
							.paymentsMethod("카드") // default
							.memo(item.memo())
							.occurredAt(item.occurredAt())
							.status(status)
							.cardLastFourDigits(item.cardLastFourDigits())
							.build();

			createdExpenses.add(expense);
		}

		// 5. 저장
		List<TemporaryExpense> savedExpenses = temporaryExpenseRepository.saveAll(createdExpenses);

		// 6. 결과 반환
		return new ParsingResult(
				meta.getTempExpenseMetaId(),
				savedExpenses.size(),
				normalCount,
				incompleteCount,
				0, // abnormalCount
				savedExpenses);
	}

	/**
	 * 파싱 항목의 상태 결정
	 */
	private TemporaryExpenseStatus determineStatus(ParsedExpenseItem item) {
		// 필수 필드 체크
		if (item.merchantName() == null || item.localAmount() == null) {
			return TemporaryExpenseStatus.INCOMPLETE;
		}

		// 선택 필드 누락 체크
		if (item.category() == null || item.occurredAt() == null) {
			return TemporaryExpenseStatus.INCOMPLETE;
		}

		return TemporaryExpenseStatus.NORMAL;
	}

	/**
	 * 카테고리 문자열 → Enum 변환
	 */
	private Category parseCategory(String categoryStr) {
		if (categoryStr == null) return null;
		try {
			return Category.valueOf(categoryStr.toUpperCase());
		} catch (IllegalArgumentException e) {
			return Category.UNCLASSIFIED;
		}
	}

	/**
	 * 통화 코드 문자열 → Enum 변환
	 */
	private CurrencyCode parseCurrencyCode(String currencyStr) {
		if (currencyStr == null) return CurrencyCode.KRW; // default
		try {
			return CurrencyCode.valueOf(currencyStr.toUpperCase());
		} catch (IllegalArgumentException e) {
			return CurrencyCode.KRW;
		}
	}

	/**
	 * 여러 파일 비동기 파싱 (SSE 진행 상황 알림)
	 */
	@org.springframework.scheduling.annotation.Async("parsingExecutor")
	public java.util.concurrent.CompletableFuture<BatchParsingResult> parseBatchFilesAsync(
			List<Long> fileIds, String taskId) {
		log.info("Starting async batch parsing for task: {}, files: {}", taskId, fileIds.size());

		int totalFiles = fileIds.size();
		int completedFiles = 0;
		int totalParsed = 0;
		int totalNormal = 0;
		int totalIncomplete = 0;
		Long firstMetaId = null;

		for (Long fileId : fileIds) {
			try {
				// 파일 조회
				File file = fileRepository.findById(fileId).orElse(null);
				if (file == null) {
					log.warn("File not found: {}", fileId);
					completedFiles++;
					continue;
				}

				// 진행 상황 알림
				progressPublisher.publishProgress(
						taskId,
						new ParsingProgressPublisher.ParsingProgressEvent(
								completedFiles,
								totalFiles,
								file.getS3Key(),
								(completedFiles * 100) / totalFiles));

				// 파싱 실행
				ParsingResult result = parseFile(fileId);
				if (firstMetaId == null) {
					firstMetaId = result.metaId();
				}
				totalParsed += result.totalCount();
				totalNormal += result.normalCount();
				totalIncomplete += result.incompleteCount();

				completedFiles++;

			} catch (Exception e) {
				log.error("Failed to parse file: {}", fileId, e);
				completedFiles++;
			}
		}

		BatchParsingResult finalResult =
				new BatchParsingResult(firstMetaId, totalParsed, totalNormal, totalIncomplete, 0);

		// 완료 이벤트 publish
		progressPublisher.complete(taskId, finalResult);

		return java.util.concurrent.CompletableFuture.completedFuture(finalResult);
	}

	/**
	 * 파싱 결과
	 */
	public record ParsingResult(
			Long metaId,
			int totalCount,
			int normalCount,
			int incompleteCount,
			int abnormalCount,
			List<TemporaryExpense> expenses) {}

	/**
	 * Batch 파싱 결과
	 */
	public record BatchParsingResult(
			Long metaId,
			int totalParsed,
			int normalCount,
			int incompleteCount,
			int abnormalCount) {}
}
