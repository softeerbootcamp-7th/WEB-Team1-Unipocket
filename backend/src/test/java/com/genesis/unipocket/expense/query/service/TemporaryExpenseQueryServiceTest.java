package com.genesis.unipocket.expense.query.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus;
import com.genesis.unipocket.tempexpense.query.persistence.repository.TemporaryExpenseQueryRepository;
import com.genesis.unipocket.tempexpense.query.persistence.response.TemporaryExpenseFileRow;
import com.genesis.unipocket.tempexpense.query.persistence.response.TemporaryExpenseItemRow;
import com.genesis.unipocket.tempexpense.query.persistence.response.TemporaryExpenseMetaRow;
import com.genesis.unipocket.tempexpense.query.persistence.response.TemporaryExpenseMetaSummaryRow;
import com.genesis.unipocket.tempexpense.query.presentation.response.TemporaryExpenseMetaFilesResponse;
import com.genesis.unipocket.tempexpense.query.presentation.response.TemporaryExpenseMetaListResponse;
import com.genesis.unipocket.tempexpense.query.service.TemporaryExpenseQueryService;
import com.genesis.unipocket.tempexpense.query.service.port.TempExpenseMediaAccessService;
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

	@Mock private TemporaryExpenseQueryRepository temporaryExpenseQueryRepository;
	@Mock private TempExpenseMediaAccessService tempExpenseMediaAccessService;

	private TemporaryExpenseQueryService service;

	private static final Long ACCOUNT_BOOK_ID = 1L;

	@BeforeEach
	void setUp() {
		service =
				new TemporaryExpenseQueryService(
						temporaryExpenseQueryRepository, tempExpenseMediaAccessService);
	}

	@Test
	@DisplayName("메타 목록 조회 - 집계 결과가 정상 동작한다")
	void getTemporaryExpenseMetas_success() {
		when(temporaryExpenseQueryRepository.findMetaSummariesByAccountBookId(ACCOUNT_BOOK_ID))
				.thenReturn(
						List.of(
								new TemporaryExpenseMetaSummaryRow(
										10L, LocalDateTime.now().minusDays(1), 2L, 1L, 1L, 0L),
								new TemporaryExpenseMetaSummaryRow(
										11L, LocalDateTime.now(), 1L, 0L, 0L, 1L)));

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
		LocalDateTime createdAt = LocalDateTime.now();
		when(temporaryExpenseQueryRepository.findMetaInAccountBook(ACCOUNT_BOOK_ID, 10L))
				.thenReturn(Optional.of(new TemporaryExpenseMetaRow(10L, createdAt)));
		when(temporaryExpenseQueryRepository.findFilesByMetaId(10L))
				.thenReturn(
						List.of(
								new TemporaryExpenseFileRow(100L, 10L, "a.png", null, "IMAGE"),
								new TemporaryExpenseFileRow(
										101L, 10L, "b.png", "receipt-b.png", "IMAGE")));
		when(temporaryExpenseQueryRepository.findExpensesByFileIds(List.of(100L, 101L)))
				.thenReturn(
						List.of(
								new TemporaryExpenseItemRow(
										1L,
										10L,
										100L,
										"A",
										null,
										null,
										null,
										null,
										null,
										null,
										null,
										null,
										TemporaryExpenseStatus.NORMAL,
										null),
								new TemporaryExpenseItemRow(
										2L,
										10L,
										101L,
										"B",
										null,
										null,
										null,
										null,
										null,
										null,
										null,
										null,
										TemporaryExpenseStatus.INCOMPLETE,
										null)));

		TemporaryExpenseMetaFilesResponse result =
				service.getTemporaryExpenseMetaFiles(ACCOUNT_BOOK_ID, 10L);

		assertThat(result.tempExpenseMetaId()).isEqualTo(10L);
		assertThat(result.files()).hasSize(2);
		assertThat(result.files().get(0).fileId()).isEqualTo(100L);
		assertThat(result.files().get(0).fileName()).isEqualTo("unknown_file.png");
		assertThat(result.files().get(0).normalCount()).isEqualTo(1);
		assertThat(result.files().get(0).incompleteCount()).isEqualTo(0);
		assertThat(result.files().get(0).abnormalCount()).isEqualTo(0);
		assertThat(result.files().get(0).expenses()).hasSize(1);
		assertThat(result.files().get(1).fileId()).isEqualTo(101L);
		assertThat(result.files().get(1).fileName()).isEqualTo("receipt-b.png");
		assertThat(result.files().get(1).normalCount()).isEqualTo(0);
		assertThat(result.files().get(1).incompleteCount()).isEqualTo(1);
		assertThat(result.files().get(1).abnormalCount()).isEqualTo(0);
		assertThat(result.files().get(1).expenses()).hasSize(1);
	}

	@Test
	@DisplayName("메타 파일 상세 조회 - 다른 가계부 메타면 scope mismatch")
	void getTemporaryExpenseMetaFiles_scopeMismatch() {
		when(temporaryExpenseQueryRepository.findMetaInAccountBook(ACCOUNT_BOOK_ID, 10L))
				.thenReturn(Optional.empty());
		when(temporaryExpenseQueryRepository.findMetaById(10L))
				.thenReturn(Optional.of(new TemporaryExpenseMetaRow(10L, LocalDateTime.now())));

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
		when(temporaryExpenseQueryRepository.findMetaInAccountBook(ACCOUNT_BOOK_ID, 10L))
				.thenReturn(Optional.empty());
		when(temporaryExpenseQueryRepository.findMetaById(10L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getTemporaryExpenseMetaFiles(ACCOUNT_BOOK_ID, 10L))
				.isInstanceOf(BusinessException.class)
				.satisfies(
						ex ->
								assertThat(((BusinessException) ex).getCode())
										.isEqualTo(ErrorCode.TEMP_EXPENSE_META_NOT_FOUND));
	}
}
