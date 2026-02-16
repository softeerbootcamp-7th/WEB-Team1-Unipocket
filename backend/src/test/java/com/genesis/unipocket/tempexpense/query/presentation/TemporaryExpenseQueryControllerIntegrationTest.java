package com.genesis.unipocket.tempexpense.query.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.genesis.unipocket.TestcontainersConfiguration;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookCreateArgs;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.command.persistence.repository.AccountBookCommandRepository;
import com.genesis.unipocket.auth.support.JwtTestHelper;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File.FileType;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus;
import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test-it")
@Import(TestcontainersConfiguration.class)
@Transactional
@Tag("integration")
class TemporaryExpenseQueryControllerIntegrationTest {

	@Autowired private MockMvc mockMvc;
	@Autowired private JwtTestHelper jwtTestHelper;
	@Autowired private UserCommandRepository userRepository;
	@Autowired private AccountBookCommandRepository accountBookRepository;
	@Autowired private TempExpenseMetaRepository tempExpenseMetaRepository;
	@Autowired private FileRepository fileRepository;
	@Autowired private TemporaryExpenseRepository temporaryExpenseRepository;

	private UUID userId;
	private Long accountBookId;
	private Long metaId;
	private Long fileId;

	@BeforeEach
	void setUp() {
		UserEntity user =
				userRepository.save(
						UserEntity.builder()
								.email("query-temp-expense@unipocket.com")
								.name("query-temp-expense")
								.mainBucketId(1L)
								.build());
		userId = user.getId();

		AccountBookEntity accountBook =
				accountBookRepository.save(
						AccountBookEntity.create(
								new AccountBookCreateArgs(
										user,
										"temp-query-book",
										CountryCode.KR,
										CountryCode.KR,
										1,
										null,
										LocalDate.of(2026, 1, 1),
										LocalDate.of(2026, 12, 31))));
		accountBookId = accountBook.getId();

		TempExpenseMeta meta =
				tempExpenseMetaRepository.save(
						TempExpenseMeta.builder()
								.accountBookId(accountBookId)
								.createdAt(LocalDateTime.of(2026, 2, 1, 10, 0))
								.build());
		metaId = meta.getTempExpenseMetaId();

		File file =
				fileRepository.save(
						File.builder()
								.tempExpenseMetaId(metaId)
								.fileType(FileType.IMAGE)
								.s3Key("temp-expenses/1/sample.png")
								.build());
		fileId = file.getFileId();

		temporaryExpenseRepository.save(
				TemporaryExpense.builder()
						.tempExpenseMetaId(metaId)
						.fileId(fileId)
						.merchantName("스타벅스")
						.category(Category.FOOD)
						.localCountryCode(CurrencyCode.KRW)
						.localCurrencyAmount(BigDecimal.valueOf(5500))
						.baseCountryCode(CurrencyCode.KRW)
						.baseCurrencyAmount(BigDecimal.valueOf(5500))
						.occurredAt(LocalDateTime.of(2026, 2, 1, 9, 30))
						.status(TemporaryExpenseStatus.NORMAL)
						.build());
	}

	@Test
	@DisplayName("메타 목록 조회 API는 메타 단위 집계를 반환한다")
	void getTemporaryExpenseMetas() throws Exception {
		mockMvc.perform(
						get("/account-books/{accountBookId}/temporary-expense-metas", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.metas.length()").value(1))
				.andExpect(jsonPath("$.metas[0].tempExpenseMetaId").value(metaId))
				.andExpect(jsonPath("$.metas[0].fileCount").value(1))
				.andExpect(jsonPath("$.metas[0].totalExpenses").value(1))
				.andExpect(jsonPath("$.metas[0].normalCount").value(1));
	}

	@Test
	@DisplayName("메타 파일 상세 조회 API는 파일별 임시지출 목록을 반환한다")
	void getTemporaryExpenseMetaFiles() throws Exception {
		mockMvc.perform(
						get(
										"/account-books/{accountBookId}/temporary-expense-metas/{tempExpenseMetaId}/files",
										accountBookId,
										metaId)
								.with(jwtTestHelper.withJwtAuth(userId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tempExpenseMetaId").value(metaId))
				.andExpect(jsonPath("$.files.length()").value(1))
				.andExpect(jsonPath("$.files[0].fileId").value(fileId))
				.andExpect(jsonPath("$.files[0].expenses.length()").value(1))
				.andExpect(jsonPath("$.files[0].expenses[0].tempExpenseMetaId").value(metaId))
				.andExpect(jsonPath("$.files[0].expenses[0].fileId").value(fileId));
	}

	@Test
	@DisplayName("메타 파일 단건 상세 조회 API는 파일 1건의 임시지출 목록을 반환한다")
	void getTemporaryExpenseMetaFile() throws Exception {
		mockMvc.perform(
						get(
										"/account-books/{accountBookId}/temporary-expense-metas/{tempExpenseMetaId}/files/{fileId}",
										accountBookId,
										metaId,
										fileId)
								.with(jwtTestHelper.withJwtAuth(userId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.fileId").value(fileId))
				.andExpect(jsonPath("$.expenses.length()").value(1))
				.andExpect(jsonPath("$.expenses[0].tempExpenseMetaId").value(metaId))
				.andExpect(jsonPath("$.expenses[0].fileId").value(fileId));
	}
}
