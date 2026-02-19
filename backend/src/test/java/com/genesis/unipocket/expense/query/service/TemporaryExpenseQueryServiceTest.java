package com.genesis.unipocket.expense.query.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File.FileType;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.facade.port.TempExpenseMediaAccessService;
import com.genesis.unipocket.tempexpense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus;
import com.genesis.unipocket.tempexpense.query.presentation.response.TemporaryExpenseMetaFilesResponse;
import com.genesis.unipocket.tempexpense.query.presentation.response.TemporaryExpenseMetaListResponse;
import com.genesis.unipocket.tempexpense.query.service.TemporaryExpenseQueryService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
	@Mock private TempExpenseMediaAccessService tempExpenseMediaAccessService;

	private TemporaryExpenseQueryService service;

	private static final Long ACCOUNT_BOOK_ID = 1L;

	@BeforeEach
	void setUp() {
		service =
				new TemporaryExpenseQueryService(
						temporaryExpenseRepository,
						fileRepository,
						tempExpenseMetaRepository,
						tempExpenseMediaAccessService,
						600);
	}

	@Test
	@DisplayName("메타 목록 조회 - 파일 수/상태 집계가 정상 동작한다")
	void getTemporaryExpenseMetas_success() {
		TempExpenseMeta meta1 =
				TempExpenseMeta.builder()
						.tempExpenseMetaId(10L)
						.accountBookId(ACCOUNT_BOOK_ID)
						.createdAt(LocalDateTime.now().minusDays(1))
						.build();
		TempExpenseMeta meta2 =
				TempExpenseMeta.builder()
						.tempExpenseMetaId(11L)
						.accountBookId(ACCOUNT_BOOK_ID)
						.createdAt(LocalDateTime.now())
						.build();

		File file1 =
				File.builder().fileId(100L).tempExpenseMetaId(10L).fileType(FileType.IMAGE).build();
		File file2 =
				File.builder().fileId(101L).tempExpenseMetaId(10L).fileType(FileType.IMAGE).build();
		File file3 =
				File.builder().fileId(102L).tempExpenseMetaId(11L).fileType(FileType.IMAGE).build();

		TemporaryExpense normal =
				TemporaryExpense.builder()
						.tempExpenseId(1L)
						.tempExpenseMetaId(10L)
						.fileId(100L)
						.status(TemporaryExpenseStatus.NORMAL)
						.build();
		TemporaryExpense incomplete =
				TemporaryExpense.builder()
						.tempExpenseId(2L)
						.tempExpenseMetaId(10L)
						.fileId(101L)
						.status(TemporaryExpenseStatus.INCOMPLETE)
						.build();
		TemporaryExpense abnormal =
				TemporaryExpense.builder()
						.tempExpenseId(3L)
						.tempExpenseMetaId(11L)
						.fileId(102L)
						.status(TemporaryExpenseStatus.ABNORMAL)
						.build();

		when(tempExpenseMetaRepository.findByAccountBookId(ACCOUNT_BOOK_ID))
				.thenReturn(List.of(meta1, meta2));
		when(fileRepository.countFilesByTempExpenseMetaIdIn(List.of(10L, 11L)))
				.thenReturn(List.of(new Object[] {10L, 2L}, new Object[] {11L, 1L}));
		when(temporaryExpenseRepository.findByTempExpenseMetaIdIn(List.of(10L, 11L)))
				.thenReturn(List.of(normal, incomplete, abnormal));

		TemporaryExpenseMetaListResponse result = service.getTemporaryExpenseMetas(ACCOUNT_BOOK_ID);

		assertThat(result.metas()).hasSize(2);
		assertThat(result.metas().get(0).tempExpenseMetaId()).isEqualTo(10L);
		assertThat(result.metas().get(0).fileCount()).isEqualTo(2);
		assertThat(result.metas().get(0).normalCount()).isEqualTo(1);
		assertThat(result.metas().get(0).incompleteCount()).isEqualTo(1);
		assertThat(result.metas().get(1).tempExpenseMetaId()).isEqualTo(11L);
		assertThat(result.metas().get(1).abnormalCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("메타 파일 상세 조회 - fileId 기준으로 임시지출이 묶여 반환된다")
	void getTemporaryExpenseMetaFiles_success() {
		TempExpenseMeta meta =
				TempExpenseMeta.builder()
						.tempExpenseMetaId(10L)
						.accountBookId(ACCOUNT_BOOK_ID)
						.createdAt(LocalDateTime.now())
						.build();

		File file1 =
				File.builder()
						.fileId(100L)
						.tempExpenseMetaId(10L)
						.fileType(FileType.IMAGE)
						.s3Key("a.png")
						.build();
		File file2 =
				File.builder()
						.fileId(101L)
						.tempExpenseMetaId(10L)
						.fileType(FileType.IMAGE)
						.s3Key("b.png")
						.build();

		TemporaryExpense e1 =
				TemporaryExpense.builder()
						.tempExpenseId(1L)
						.tempExpenseMetaId(10L)
						.fileId(100L)
						.merchantName("A")
						.status(TemporaryExpenseStatus.NORMAL)
						.build();
		TemporaryExpense e2 =
				TemporaryExpense.builder()
						.tempExpenseId(2L)
						.tempExpenseMetaId(10L)
						.fileId(101L)
						.merchantName("B")
						.status(TemporaryExpenseStatus.INCOMPLETE)
						.build();

		when(tempExpenseMetaRepository.findById(10L)).thenReturn(Optional.of(meta));
		when(fileRepository.findByTempExpenseMetaId(10L)).thenReturn(List.of(file1, file2));
		when(temporaryExpenseRepository.findByFileIdIn(List.of(100L, 101L)))
				.thenReturn(List.of(e1, e2));

		TemporaryExpenseMetaFilesResponse result =
				service.getTemporaryExpenseMetaFiles(ACCOUNT_BOOK_ID, 10L);

		assertThat(result.tempExpenseMetaId()).isEqualTo(10L);
		assertThat(result.files()).hasSize(2);
		assertThat(result.files().get(0).fileId()).isEqualTo(100L);
		assertThat(result.files().get(0).expenses()).hasSize(1);
		assertThat(result.files().get(1).fileId()).isEqualTo(101L);
		assertThat(result.files().get(1).expenses()).hasSize(1);
	}

	@Test
	@DisplayName("메타 파일 상세 조회 - 다른 가계부 메타면 scope mismatch")
	void getTemporaryExpenseMetaFiles_scopeMismatch() {
		TempExpenseMeta meta =
				TempExpenseMeta.builder().tempExpenseMetaId(10L).accountBookId(999L).build();
		when(tempExpenseMetaRepository.findById(10L)).thenReturn(Optional.of(meta));

		assertThatThrownBy(() -> service.getTemporaryExpenseMetaFiles(ACCOUNT_BOOK_ID, 10L))
				.isInstanceOf(BusinessException.class)
				.satisfies(
						ex ->
								assertThat(((BusinessException) ex).getCode())
										.isEqualTo(ErrorCode.TEMP_EXPENSE_SCOPE_MISMATCH));
	}

	@Test
	@DisplayName("메타 파일 상세 조회 - 메타가 없으면 not found")
	void getTemporaryExpenseMetaFiles_metaNotFound() {
		when(tempExpenseMetaRepository.findById(10L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getTemporaryExpenseMetaFiles(ACCOUNT_BOOK_ID, 10L))
				.isInstanceOf(BusinessException.class)
				.satisfies(
						ex ->
								assertThat(((BusinessException) ex).getCode())
										.isEqualTo(ErrorCode.TEMP_EXPENSE_META_NOT_FOUND));
	}
}
