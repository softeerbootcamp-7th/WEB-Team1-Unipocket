package com.genesis.unipocket.tempexpense.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genesis.unipocket.TestcontainersConfiguration;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookCreateArgs;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.command.persistence.repository.AccountBookCommandRepository;
import com.genesis.unipocket.auth.support.JwtTestHelper;
import com.genesis.unipocket.exchange.query.application.ExchangeRateService;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.infrastructure.gemini.GeminiService;
import com.genesis.unipocket.media.command.facade.provide.TempExpenseMediaProvider;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.support.TransactionTemplate;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test-it")
@Import(TestcontainersConfiguration.class)
@Tag("integration")
class TemporaryExpenseSseIntegrationTest {

	@Autowired private MockMvc mockMvc;
	@Autowired private ObjectMapper objectMapper;
	@Autowired private UserCommandRepository userRepository;
	@Autowired private AccountBookCommandRepository accountBookRepository;
	@Autowired private TempExpenseMetaRepository tempExpenseMetaRepository;
	@Autowired private FileRepository fileRepository;
	@Autowired private TemporaryExpenseRepository temporaryExpenseRepository;
	@Autowired private JwtTestHelper jwtTestHelper;
	@Autowired private TransactionTemplate transactionTemplate;

	@MockitoBean private GeminiService geminiService;
	@MockitoBean private ExchangeRateService exchangeRateService;
	@MockitoBean private TempExpenseMediaProvider tempExpenseMediaProvider;

	private Long accountBookId;
	private Long tempExpenseMetaId;
	private String s3Key;
	private UUID userId;

	@BeforeEach
	void setUp() {
		UserEntity user =
				userRepository.save(
						UserEntity.builder()
								.email("tempexpense-sse@unipocket.com")
								.name("tempexpense-sse")
								.mainBucketId(1L)
								.build());
		userId = user.getId();

		AccountBookEntity accountBook =
				accountBookRepository.save(
						AccountBookEntity.create(
								new AccountBookCreateArgs(
										user,
										"TempExpense SSE AccountBook",
										CountryCode.KR,
										CountryCode.US,
										1,
										null,
										LocalDate.of(2026, 1, 1),
										LocalDate.of(2026, 12, 31))));
		accountBookId = accountBook.getId();

		TempExpenseMeta meta =
				tempExpenseMetaRepository.save(
						TempExpenseMeta.builder().accountBookId(accountBookId).build());
		tempExpenseMetaId = meta.getTempExpenseMetaId();

		s3Key = "temp-expenses/" + accountBookId + "/integration-sse-image.png";
		fileRepository.save(
				File.builder()
						.tempExpenseMetaId(tempExpenseMetaId)
						.fileType(File.FileType.IMAGE)
						.s3Key(s3Key)
						.build());
	}

	@AfterEach
	void cleanAsyncPersistedData() {
		transactionTemplate.executeWithoutResult(
				status -> {
					temporaryExpenseRepository.deleteByTempExpenseMetaId(tempExpenseMetaId);
					fileRepository.deleteByTempExpenseMetaId(tempExpenseMetaId);
					tempExpenseMetaRepository.deleteById(tempExpenseMetaId);
					accountBookRepository.deleteById(accountBookId);
					userRepository.deleteById(userId);
				});
	}

	@Test
	@DisplayName("SSE 스트림은 progress(%) 지표와 complete 이벤트를 반환한다")
	void parseStatusStream_containsProgressPercentAndCompleteEvent() throws Exception {
		when(tempExpenseMediaProvider.issueGetPath(anyString(), any()))
				.thenReturn("https://example.com/test-receipt.png");
		when(geminiService.parseReceiptImage(anyString(), anyString()))
				.thenReturn(
						new GeminiService.GeminiParseResponse(
								true,
								List.of(
										new GeminiService.ParsedExpenseItem(
												"SSE Test Merchant",
												"FOOD",
												new BigDecimal("10.00"),
												"USD",
												null,
												null,
												LocalDateTime.of(2026, 2, 16, 12, 30),
												"1234",
												"A-100",
												"integration test")),
								null));
		when(exchangeRateService.getExchangeRate(any(), any(), any()))
				.thenReturn(new BigDecimal("1300.00"));

		String parseBody =
				objectMapper.writeValueAsString(
						new ParseRequest(tempExpenseMetaId, List.of(s3Key)));
		MvcResult parseResult =
				mockMvc.perform(
								post(
												"/account-books/{accountBookId}/temporary-expenses/parse",
												accountBookId)
										.with(jwtTestHelper.withJwtAuth(userId))
										.contentType(MediaType.APPLICATION_JSON)
										.content(parseBody))
						.andReturn();

		assertThat(parseResult.getResponse().getStatus()).isEqualTo(202);
		JsonNode parseJson = objectMapper.readTree(parseResult.getResponse().getContentAsString());
		String taskId = parseJson.path("taskId").asText();
		assertThat(taskId).isNotBlank();

		MvcResult sseResult =
				mockMvc.perform(
								get(
												"/account-books/{accountBookId}/temporary-expenses/parse-status/{taskId}",
												accountBookId,
												taskId)
										.with(jwtTestHelper.withJwtAuth(userId)))
						.andReturn();

		if (sseResult.getRequest().isAsyncStarted()) {
			sseResult = mockMvc.perform(asyncDispatch(sseResult)).andReturn();
		}

		assertThat(sseResult.getResponse().getStatus()).isEqualTo(200);

		String sseBody = sseResult.getResponse().getContentAsString();
		assertThat(sseBody).contains("event:progress");
		assertThat(sseBody).contains("event:complete");
		assertThat(sseBody).contains("\"progress\":100");

		assertThat(temporaryExpenseRepository.findByTempExpenseMetaId(tempExpenseMetaId))
				.hasSize(1);
	}

	@Test
	@DisplayName("SSE 스트림은 진행/완료 상태가 없으면 404를 반환한다")
	void parseStatusStream_returnsNotFoundWhenTaskMissing() throws Exception {
		MvcResult result =
				mockMvc.perform(
								get(
												"/account-books/{accountBookId}/temporary-expenses/parse-status/{taskId}",
												accountBookId,
												UUID.randomUUID())
										.with(jwtTestHelper.withJwtAuth(userId)))
						.andReturn();

		assertThat(result.getResponse().getStatus()).isEqualTo(404);
	}

	private record ParseRequest(Long tempExpenseMetaId, List<String> s3Keys) {}
}
