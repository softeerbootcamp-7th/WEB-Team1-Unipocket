package com.genesis.unipocket.tempexpense.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.expense.command.persistence.entity.ExpenseEntity;
import com.genesis.unipocket.expense.command.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.tempexpense.command.application.result.ConfirmStartResult;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus;
import com.genesis.unipocket.tempexpense.common.exception.TempExpenseConvertValidationException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TemporaryExpenseConversionServiceTest {

	@Mock private TemporaryExpenseRepository tempExpenseRepository;
	@Mock private FileRepository fileRepository;
	@Mock private ExpenseRepository expenseRepository;

	private TemporaryExpenseConversionService service;

	@BeforeEach
	void setUp() {
		service =
				new TemporaryExpenseConversionService(
						tempExpenseRepository, fileRepository, expenseRepository);
	}

	@Test
	@DisplayName("convertMetaToExpenses는 메타 내 NORMAL 임시지출을 동기 변환 후 삭제한다")
	void convertMetaToExpenses_convertsAndDeletesAll() {
		Long accountBookId = 1L;
		Long metaId = 10L;
		TemporaryExpense temp =
				TemporaryExpense.builder()
						.tempExpenseId(100L)
						.tempExpenseMetaId(metaId)
						.fileId(200L)
						.merchantName("상호")
						.category(Category.FOOD)
						.localCountryCode(CurrencyCode.USD)
						.localCurrencyAmount(new BigDecimal("10.00"))
						.baseCountryCode(CurrencyCode.KRW)
						.baseCurrencyAmount(new BigDecimal("13000.00"))
						.exchangeRate(new BigDecimal("1300.00"))
						.occurredAt(LocalDateTime.of(2026, 2, 17, 10, 0))
						.status(TemporaryExpenseStatus.NORMAL)
						.build();
		File file =
				File.builder()
						.fileId(200L)
						.tempExpenseMetaId(metaId)
						.fileType(File.FileType.IMAGE)
						.s3Key("temp-expenses/sample.png")
						.build();

		when(tempExpenseRepository.findByTempExpenseMetaId(metaId)).thenReturn(List.of(temp));
		when(fileRepository.findByTempExpenseMetaId(metaId)).thenReturn(List.of(file));
		when(expenseRepository.saveAll(anyList()))
				.thenReturn(List.of(ExpenseEntity.builder().expenseId(999L).build()));

		ConfirmStartResult result = service.convertMetaToExpenses(accountBookId, metaId);

		assertThat(result.convertedExpenses()).isEqualTo(1);
		verify(expenseRepository).saveAll(anyList());
		verify(tempExpenseRepository).deleteAll(List.of(temp));
	}

	@Test
	@DisplayName("convertMetaToExpenses는 NORMAL이 아닌 항목이 있으면 변환을 중단한다")
	void convertMetaToExpenses_throwsWhenStatusIsNotNormal() {
		Long accountBookId = 1L;
		Long metaId = 10L;
		TemporaryExpense invalid =
				TemporaryExpense.builder()
						.tempExpenseId(101L)
						.tempExpenseMetaId(metaId)
						.status(TemporaryExpenseStatus.INCOMPLETE)
						.build();

		when(tempExpenseRepository.findByTempExpenseMetaId(metaId)).thenReturn(List.of(invalid));

		assertThatThrownBy(() -> service.convertMetaToExpenses(accountBookId, metaId))
				.isInstanceOf(TempExpenseConvertValidationException.class);
	}
}
