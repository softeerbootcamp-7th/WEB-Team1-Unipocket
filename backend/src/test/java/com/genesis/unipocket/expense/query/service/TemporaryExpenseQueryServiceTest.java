package com.genesis.unipocket.expense.query.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookOwnershipValidator;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File.FileType;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense.TemporaryExpenseStatus;
import com.genesis.unipocket.tempexpense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.tempexpense.query.presentation.response.FileProcessingSummaryResponse;
import com.genesis.unipocket.tempexpense.query.presentation.response.ImageProcessingSummaryResponse;
import com.genesis.unipocket.tempexpense.query.service.TemporaryExpenseQueryService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * <b>TemporaryExpenseQueryService 단위 테스트</b>
 *
 * @since 2026-02-11
 */
@ExtendWith(MockitoExtension.class)
class TemporaryExpenseQueryServiceTest {

	@Mock private TemporaryExpenseRepository temporaryExpenseRepository;
	@Mock private FileRepository fileRepository;
	@Mock private TempExpenseMetaRepository tempExpenseMetaRepository;
	@Mock private AccountBookOwnershipValidator accountBookOwnershipValidator;

	@InjectMocks private TemporaryExpenseQueryService service;

	private static final Long ACCOUNT_BOOK_ID = 1L;
	private static final UUID USER_ID = UUID.randomUUID();

	@Test
	@DisplayName("파일별 처리 현황 조회 - 모든 항목이 NORMAL이면 processed=true")
	void getFileProcessingSummary_allNormal_processed() {
		// given
		TempExpenseMeta meta =
				TempExpenseMeta.builder()
						.tempExpenseMetaId(10L)
						.accountBookId(ACCOUNT_BOOK_ID)
						.build();
		File file =
				File.builder()
						.fileId(100L)
						.tempExpenseMetaId(10L)
						.fileType(FileType.IMAGE)
						.s3Key("test/image.jpg")
						.build();
		TemporaryExpense expense =
				TemporaryExpense.builder()
						.tempExpenseId(1000L)
						.tempExpenseMetaId(10L)
						.merchantName("스타벅스")
						.category(Category.FOOD)
						.localCurrencyAmount(BigDecimal.valueOf(5000))
						.occurredAt(LocalDateTime.now())
						.status(TemporaryExpenseStatus.NORMAL)
						.build();

		when(tempExpenseMetaRepository.findByAccountBookId(ACCOUNT_BOOK_ID))
				.thenReturn(List.of(meta));
		when(fileRepository.findByTempExpenseMetaIdIn(List.of(10L))).thenReturn(List.of(file));
		when(temporaryExpenseRepository.findByTempExpenseMetaIdIn(List.of(10L)))
				.thenReturn(List.of(expense));

		// when
		FileProcessingSummaryResponse result =
				service.getFileProcessingSummary(ACCOUNT_BOOK_ID, USER_ID);

		// then
		assertThat(result.totalFiles()).isEqualTo(1);
		assertThat(result.processedFiles()).isEqualTo(1);
		assertThat(result.unprocessedFiles()).isEqualTo(0);
		assertThat(result.files()).hasSize(1);
		assertThat(result.files().get(0).processed()).isTrue();
		assertThat(result.files().get(0).normalCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("파일별 처리 현황 조회 - INCOMPLETE 항목이 있으면 processed=false")
	void getFileProcessingSummary_hasIncomplete_notProcessed() {
		// given
		TempExpenseMeta meta =
				TempExpenseMeta.builder()
						.tempExpenseMetaId(10L)
						.accountBookId(ACCOUNT_BOOK_ID)
						.build();
		File file =
				File.builder()
						.fileId(100L)
						.tempExpenseMetaId(10L)
						.fileType(FileType.IMAGE)
						.s3Key("test/image.jpg")
						.build();
		TemporaryExpense normalExpense =
				TemporaryExpense.builder()
						.tempExpenseId(1000L)
						.tempExpenseMetaId(10L)
						.merchantName("스타벅스")
						.status(TemporaryExpenseStatus.NORMAL)
						.build();
		TemporaryExpense incompleteExpense =
				TemporaryExpense.builder()
						.tempExpenseId(1001L)
						.tempExpenseMetaId(10L)
						.merchantName("이디야")
						.status(TemporaryExpenseStatus.INCOMPLETE)
						.build();

		when(tempExpenseMetaRepository.findByAccountBookId(ACCOUNT_BOOK_ID))
				.thenReturn(List.of(meta));
		when(fileRepository.findByTempExpenseMetaIdIn(List.of(10L))).thenReturn(List.of(file));
		when(temporaryExpenseRepository.findByTempExpenseMetaIdIn(List.of(10L)))
				.thenReturn(List.of(normalExpense, incompleteExpense));

		// when
		FileProcessingSummaryResponse result =
				service.getFileProcessingSummary(ACCOUNT_BOOK_ID, USER_ID);

		// then
		assertThat(result.processedFiles()).isEqualTo(0);
		assertThat(result.unprocessedFiles()).isEqualTo(1);
		assertThat(result.files().get(0).processed()).isFalse();
		assertThat(result.files().get(0).normalCount()).isEqualTo(1);
		assertThat(result.files().get(0).incompleteCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("파일별 처리 현황 조회 - 메타가 없으면 빈 결과")
	void getFileProcessingSummary_noMeta_emptyResult() {
		// given
		when(tempExpenseMetaRepository.findByAccountBookId(ACCOUNT_BOOK_ID)).thenReturn(List.of());

		// when
		FileProcessingSummaryResponse result =
				service.getFileProcessingSummary(ACCOUNT_BOOK_ID, USER_ID);

		// then
		assertThat(result.totalFiles()).isEqualTo(0);
		assertThat(result.processedFiles()).isEqualTo(0);
		assertThat(result.files()).isEmpty();
	}

	@Test
	@DisplayName("이미지 처리 현황 요약 - 정상 동작")
	void getImageProcessingSummary_success() {
		// given
		TempExpenseMeta meta1 =
				TempExpenseMeta.builder()
						.tempExpenseMetaId(10L)
						.accountBookId(ACCOUNT_BOOK_ID)
						.build();
		TempExpenseMeta meta2 =
				TempExpenseMeta.builder()
						.tempExpenseMetaId(11L)
						.accountBookId(ACCOUNT_BOOK_ID)
						.build();
		File file1 =
				File.builder()
						.fileId(100L)
						.tempExpenseMetaId(10L)
						.fileType(FileType.IMAGE)
						.s3Key("img1.jpg")
						.build();
		File file2 =
				File.builder()
						.fileId(101L)
						.tempExpenseMetaId(11L)
						.fileType(FileType.IMAGE)
						.s3Key("img2.jpg")
						.build();

		TemporaryExpense normal1 =
				TemporaryExpense.builder()
						.tempExpenseId(1L)
						.tempExpenseMetaId(10L)
						.status(TemporaryExpenseStatus.NORMAL)
						.merchantName("A")
						.build();
		TemporaryExpense normal2 =
				TemporaryExpense.builder()
						.tempExpenseId(2L)
						.tempExpenseMetaId(10L)
						.status(TemporaryExpenseStatus.NORMAL)
						.merchantName("B")
						.build();
		TemporaryExpense incomplete =
				TemporaryExpense.builder()
						.tempExpenseId(3L)
						.tempExpenseMetaId(11L)
						.status(TemporaryExpenseStatus.INCOMPLETE)
						.merchantName("C")
						.build();

		when(tempExpenseMetaRepository.findByAccountBookId(ACCOUNT_BOOK_ID))
				.thenReturn(List.of(meta1, meta2));
		when(fileRepository.findByTempExpenseMetaIdIn(List.of(10L, 11L)))
				.thenReturn(List.of(file1, file2));
		when(temporaryExpenseRepository.findByTempExpenseMetaIdIn(List.of(10L, 11L)))
				.thenReturn(List.of(normal1, normal2, incomplete));

		// when
		ImageProcessingSummaryResponse result =
				service.getImageProcessingSummary(ACCOUNT_BOOK_ID, USER_ID);

		// then
		assertThat(result.totalImages()).isEqualTo(2);
		assertThat(result.processedImages()).isEqualTo(1); // file1만 모두 NORMAL
		assertThat(result.unprocessedImages()).isEqualTo(1); // file2에 INCOMPLETE
		assertThat(result.totalExpenses()).isEqualTo(3);
		assertThat(result.normalExpenses()).isEqualTo(2);
		assertThat(result.incompleteExpenses()).isEqualTo(1);
		assertThat(result.abnormalExpenses()).isEqualTo(0);
	}

	@Test
	@DisplayName("이미지 처리 현황 요약 - 메타가 없으면 전부 0")
	void getImageProcessingSummary_noMeta_zeros() {
		// given
		when(tempExpenseMetaRepository.findByAccountBookId(ACCOUNT_BOOK_ID)).thenReturn(List.of());

		// when
		ImageProcessingSummaryResponse result =
				service.getImageProcessingSummary(ACCOUNT_BOOK_ID, USER_ID);

		// then
		assertThat(result.totalImages()).isEqualTo(0);
		assertThat(result.processedImages()).isEqualTo(0);
		assertThat(result.totalExpenses()).isEqualTo(0);
	}
}
