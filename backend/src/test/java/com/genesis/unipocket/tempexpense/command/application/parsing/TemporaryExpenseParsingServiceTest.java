package com.genesis.unipocket.tempexpense.command.application.parsing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.infrastructure.gemini.GeminiService;
import com.genesis.unipocket.tempexpense.command.application.result.ParseStartResult;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookRateInfoProvider;
import com.genesis.unipocket.tempexpense.command.facade.provide.TemporaryExpenseScopeValidationProvider;
import com.genesis.unipocket.tempexpense.command.facade.port.ExchangeRateProvider;
import com.genesis.unipocket.tempexpense.command.facade.port.dto.AccountBookRateInfo;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus;
import com.genesis.unipocket.tempexpense.common.infrastructure.sse.ParsingProgressPublisher;
import com.genesis.unipocket.tempexpense.common.validation.TemporaryExpenseValidator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TemporaryExpenseParsingServiceTest {

	@Mock private FileRepository fileRepository;
	@Mock private TemporaryExpenseRepository temporaryExpenseRepository;
	@Mock private AccountBookRateInfoProvider accountBookRateInfoProvider;
	@Mock private ExchangeRateProvider exchangeRateProvider;
	@Mock private TemporaryExpenseParseClient temporaryExpenseParseClient;
	@Mock private TemporaryExpenseFieldParser fieldParser;
	@Mock private ParsingProgressPublisher progressPublisher;
	@Mock private TemporaryExpenseScopeValidationProvider temporaryExpenseScopeValidator;

	private TemporaryExpenseParsingService service;

	@BeforeEach
	void setUp() {
		TemporaryExpensePersistenceService temporaryExpensePersistenceService =
				new TemporaryExpensePersistenceService(
						temporaryExpenseRepository, new TemporaryExpenseValidator());
		service =
				new TemporaryExpenseParsingService(
						fileRepository,
						accountBookRateInfoProvider,
						exchangeRateProvider,
						fieldParser,
						temporaryExpenseParseClient,
						temporaryExpensePersistenceService,
						progressPublisher,
						temporaryExpenseScopeValidator,
						Runnable::run);
	}

	@Test
	@DisplayName("파싱 시 카테고리 숫자/별칭을 변환하고 환율 조회는 키별 1회만 수행")
	void parseBatchSingleFile_mapsCategoryAndDeduplicatesExchangeRateLookup() {
		Long accountBookId = 1L;
		String s3Key = "temp/image-1.jpg";
		File file =
				File.builder()
						.fileId(1L)
						.tempExpenseMetaId(10L)
						.fileType(File.FileType.IMAGE)
						.s3Key(s3Key)
						.build();
		TempExpenseMeta meta =
				TempExpenseMeta.builder()
						.tempExpenseMetaId(10L)
						.accountBookId(accountBookId)
						.build();

		when(accountBookRateInfoProvider.getRateInfo(accountBookId))
				.thenReturn(new AccountBookRateInfo(CurrencyCode.KRW, CurrencyCode.JPY));
		when(fieldParser.parseCategory(anyString())).thenCallRealMethod();
		when(fieldParser.parseCurrencyCode(eq("JPY"), any())).thenReturn(CurrencyCode.JPY);
		when(fieldParser.parseCurrencyCode(isNull(), any())).thenAnswer(i -> i.getArgument(1));

		when(temporaryExpenseParseClient.parse(file))
				.thenReturn(
						new GeminiService.GeminiParseResponse(
								true,
								List.of(
										new GeminiService.ParsedExpenseItem(
												"편의점",
												"2",
												new BigDecimal("1000"),
												"JPY",
												null,
												null,
												LocalDateTime.of(2026, 2, 1, 10, 0),
												null,
												null,
												null),
										new GeminiService.ParsedExpenseItem(
												"카페",
												"TRANSPORTATION",
												new BigDecimal("2000"),
												null,
												null,
												null,
												LocalDateTime.of(2026, 2, 1, 14, 0),
												null,
												null,
												null)),
								null));
		when(exchangeRateProvider.getExchangeRate(
						CurrencyCode.JPY,
						CurrencyCode.KRW,
						LocalDateTime.of(2026, 2, 1, 0, 0).atOffset(ZoneOffset.UTC)))
				.thenReturn(new BigDecimal("9.50"));
		when(temporaryExpenseRepository.saveAll(anyList()))
				.thenAnswer(invocation -> invocation.getArgument(0));

		service.parseBatchFiles(meta, List.of(file), "task-single-1");
		verify(exchangeRateProvider, times(1))
				.getExchangeRate(
						CurrencyCode.JPY,
						CurrencyCode.KRW,
						LocalDateTime.of(2026, 2, 1, 0, 0).atOffset(ZoneOffset.UTC));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<TemporaryExpense>> captor = ArgumentCaptor.forClass(List.class);
		verify(temporaryExpenseRepository).saveAll(captor.capture());
		List<TemporaryExpense> saved = captor.getValue();

		assertThat(saved).hasSize(2);
		assertThat(saved.get(0).getStatus()).isEqualTo(TemporaryExpenseStatus.NORMAL);
		assertThat(saved.get(1).getStatus()).isEqualTo(TemporaryExpenseStatus.NORMAL);
		assertThat(saved.get(0).getCategory()).isEqualTo(Category.FOOD);
		assertThat(saved.get(1).getCategory()).isEqualTo(Category.TRANSPORT);
		assertThat(saved.get(1).getLocalCountryCode()).isEqualTo(CurrencyCode.JPY);
		assertThat(saved.get(0).getBaseCountryCode()).isEqualTo(CurrencyCode.KRW);
		assertThat(saved.get(0).getBaseCurrencyAmount()).isEqualByComparingTo("9500.00");
		assertThat(saved.get(1).getBaseCurrencyAmount()).isEqualByComparingTo("19000.00");
	}

	@Test
	@DisplayName("거래일시가 없으면 INCOMPLETE로 저장되고 환율 조회는 수행하지 않음")
	void parseBatchSingleFile_incompleteItemSkipsExchangeLookup() {
		Long accountBookId = 1L;
		String s3Key = "temp/image-2.jpg";
		File file =
				File.builder()
						.fileId(2L)
						.tempExpenseMetaId(11L)
						.fileType(File.FileType.IMAGE)
						.s3Key(s3Key)
						.build();
		TempExpenseMeta meta =
				TempExpenseMeta.builder()
						.tempExpenseMetaId(11L)
						.accountBookId(accountBookId)
						.build();

		when(accountBookRateInfoProvider.getRateInfo(accountBookId))
				.thenReturn(new AccountBookRateInfo(CurrencyCode.KRW, CurrencyCode.JPY));
		when(fieldParser.parseCategory(anyString())).thenCallRealMethod();
		when(fieldParser.parseCurrencyCode(eq("JPY"), any())).thenReturn(CurrencyCode.JPY);
		when(fieldParser.parseCurrencyCode(isNull(), any())).thenAnswer(i -> i.getArgument(1));

		when(temporaryExpenseParseClient.parse(file))
				.thenReturn(
						new GeminiService.GeminiParseResponse(
								true,
								List.of(
										new GeminiService.ParsedExpenseItem(
												"마트",
												"1",
												new BigDecimal("5000"),
												"JPY",
												null,
												null,
												null,
												null,
												null,
												null)),
								null));
		when(temporaryExpenseRepository.saveAll(anyList()))
				.thenAnswer(invocation -> invocation.getArgument(0));

		service.parseBatchFiles(meta, List.of(file), "task-single-2");
		verifyNoInteractions(exchangeRateProvider);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<TemporaryExpense>> captor = ArgumentCaptor.forClass(List.class);
		verify(temporaryExpenseRepository).saveAll(captor.capture());
		TemporaryExpense saved = captor.getValue().get(0);
		assertThat(saved.getStatus()).isEqualTo(TemporaryExpenseStatus.INCOMPLETE);
		assertThat(saved.getBaseCurrencyAmount()).isNull();
	}

	@Test
	@DisplayName("파싱 결과에 기본 통화 금액(BaseAmount)이 있으면 환율 조회를 건너뛰고 해당 값을 사용")
	void parseBatchSingleFile_usesProvidedBaseAmount_skipsExchangeLookup() {
		Long accountBookId = 1L;
		String s3Key = "temp/image-base-amount.jpg";
		File file =
				File.builder()
						.fileId(3L)
						.tempExpenseMetaId(12L)
						.fileType(File.FileType.IMAGE)
						.s3Key(s3Key)
						.build();
		TempExpenseMeta meta =
				TempExpenseMeta.builder()
						.tempExpenseMetaId(12L)
						.accountBookId(accountBookId)
						.build();

		when(accountBookRateInfoProvider.getRateInfo(accountBookId))
				.thenReturn(new AccountBookRateInfo(CurrencyCode.KRW, CurrencyCode.JPY));
		when(fieldParser.parseCategory(anyString())).thenCallRealMethod();
		when(fieldParser.parseCurrencyCode(eq("USD"), any())).thenReturn(CurrencyCode.USD);
		when(fieldParser.parseCurrencyCode(eq("KRW"), any())).thenReturn(CurrencyCode.KRW);

		when(temporaryExpenseParseClient.parse(file))
				.thenReturn(
						new GeminiService.GeminiParseResponse(
								true,
								List.of(
										new GeminiService.ParsedExpenseItem(
												"Global Store",
												"SHOPPING",
												new BigDecimal("100.00"),
												"USD",
												new BigDecimal("135000"), // Provided Base Amount
												"KRW", // Base Currency matches AccountBook
												LocalDateTime.of(2026, 2, 15, 12, 0),
												null,
												null,
												null)),
								null));

		when(temporaryExpenseRepository.saveAll(anyList()))
				.thenAnswer(invocation -> invocation.getArgument(0));

		service.parseBatchFiles(meta, List.of(file), "task-single-3");

		// Verify NO interaction with ExchangeRateProvider
		verifyNoInteractions(exchangeRateProvider);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<TemporaryExpense>> captor = ArgumentCaptor.forClass(List.class);
		verify(temporaryExpenseRepository).saveAll(captor.capture());
		TemporaryExpense saved = captor.getValue().get(0);

		assertThat(saved.getStatus()).isEqualTo(TemporaryExpenseStatus.NORMAL);
		assertThat(saved.getLocalCurrencyAmount()).isEqualByComparingTo("100.00");
		assertThat(saved.getLocalCountryCode()).isEqualTo(CurrencyCode.USD);
		assertThat(saved.getBaseCountryCode()).isEqualTo(CurrencyCode.KRW);
		assertThat(saved.getBaseCurrencyAmount())
				.isEqualByComparingTo("135000"); // Should use the provided value
	}

	@Test
	@DisplayName("배치 파싱은 전달된 파일 목록을 순회해 처리한다")
	void parseBatchFiles_iteratesOverGivenFiles() {
		Long accountBookId = 1L;
		File fileA =
				File.builder()
						.fileId(1L)
						.tempExpenseMetaId(100L)
						.fileType(File.FileType.CSV)
						.s3Key("docs/a.csv")
						.build();
		File fileB =
				File.builder()
						.fileId(2L)
						.tempExpenseMetaId(100L)
						.fileType(File.FileType.CSV)
						.s3Key("docs/b.csv")
						.build();
		TempExpenseMeta meta =
				TempExpenseMeta.builder()
						.tempExpenseMetaId(100L)
						.accountBookId(accountBookId)
						.build();

		when(accountBookRateInfoProvider.getRateInfo(accountBookId))
				.thenReturn(new AccountBookRateInfo(CurrencyCode.KRW, CurrencyCode.JPY));
		when(temporaryExpenseParseClient.parse(any(File.class)))
				.thenReturn(new GeminiService.GeminiParseResponse(true, List.of(), null));
		when(temporaryExpenseRepository.saveAll(anyList()))
				.thenAnswer(invocation -> invocation.getArgument(0));

		service.parseBatchFiles(meta, List.of(fileA, fileB), "task-1");

		verify(temporaryExpenseRepository, atLeastOnce()).saveAll(anyList());
		verify(temporaryExpenseParseClient, times(2)).parse(any(File.class));
		verify(progressPublisher, times(1)).complete(eq("task-1"));
	}

	@Test
	@DisplayName("startParseAsync는 s3Keys가 null이어도 NPE 없이 메타 전체 파일을 대상으로 시작한다")
	void startParseAsync_acceptsNullS3Keys() {
		TemporaryExpenseParsingService asyncStartOnlyService =
				new TemporaryExpenseParsingService(
						fileRepository,
						accountBookRateInfoProvider,
						exchangeRateProvider,
						fieldParser,
						temporaryExpenseParseClient,
						new TemporaryExpensePersistenceService(
								temporaryExpenseRepository, new TemporaryExpenseValidator()),
						progressPublisher,
						temporaryExpenseScopeValidator,
						runnable -> {});

		Long accountBookId = 1L;
		Long metaId = 10L;
		TempExpenseMeta meta =
				TempExpenseMeta.builder()
						.tempExpenseMetaId(metaId)
						.accountBookId(accountBookId)
						.build();
		File file1 =
				File.builder()
						.fileId(1L)
						.tempExpenseMetaId(metaId)
						.fileType(File.FileType.IMAGE)
						.s3Key("temp/a.png")
						.build();
		File file2 =
				File.builder()
						.fileId(2L)
						.tempExpenseMetaId(metaId)
						.fileType(File.FileType.IMAGE)
						.s3Key("temp/b.png")
						.build();

		when(temporaryExpenseScopeValidator.validateMetaScope(accountBookId, metaId))
				.thenReturn(meta);
		when(fileRepository.findByTempExpenseMetaId(metaId)).thenReturn(List.of(file1, file2));

		ParseStartResult result =
				asyncStartOnlyService.startParseAsync(accountBookId, metaId, null);

		assertThat(result.totalFiles()).isEqualTo(2);
		verify(progressPublisher).registerTask(anyString(), eq(accountBookId));
	}

	@Test
	@DisplayName("baseAmount는 baseCurrencyCode가 없으면 무시되고 환율로 재계산한다")
	void parseBatchSingleFile_ignoresBaseAmountWhenBaseCurrencyCodeMissing() {
		Long accountBookId = 1L;
		String s3Key = "temp/image-base-missing-currency.jpg";
		File file =
				File.builder()
						.fileId(4L)
						.tempExpenseMetaId(13L)
						.fileType(File.FileType.IMAGE)
						.s3Key(s3Key)
						.build();
		TempExpenseMeta meta =
				TempExpenseMeta.builder()
						.tempExpenseMetaId(13L)
						.accountBookId(accountBookId)
						.build();

		when(accountBookRateInfoProvider.getRateInfo(accountBookId))
				.thenReturn(new AccountBookRateInfo(CurrencyCode.KRW, CurrencyCode.USD));
		when(fieldParser.parseCategory(anyString())).thenCallRealMethod();
		when(fieldParser.parseCurrencyCode(eq("USD"), any())).thenReturn(CurrencyCode.USD);
		when(fieldParser.parseCurrencyCode(isNull(), any())).thenAnswer(i -> i.getArgument(1));
		when(exchangeRateProvider.getExchangeRate(
						eq(CurrencyCode.USD), eq(CurrencyCode.KRW), any()))
				.thenReturn(new BigDecimal("1300"));
		when(temporaryExpenseRepository.saveAll(anyList()))
				.thenAnswer(invocation -> invocation.getArgument(0));

		when(temporaryExpenseParseClient.parse(file))
				.thenReturn(
						new GeminiService.GeminiParseResponse(
								true,
								List.of(
										new GeminiService.ParsedExpenseItem(
												"Global Store",
												"SHOPPING",
												new BigDecimal("100.00"),
												"USD",
												new BigDecimal(
														"1000.00"), // provided but invalid without
												// currency
												null,
												LocalDateTime.of(2026, 2, 15, 12, 0),
												null,
												null,
												null)),
								null));

		service.parseBatchFiles(meta, List.of(file), "task-single-4");
		verify(exchangeRateProvider, atLeastOnce())
				.getExchangeRate(eq(CurrencyCode.USD), eq(CurrencyCode.KRW), any());

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<TemporaryExpense>> captor = ArgumentCaptor.forClass(List.class);
		verify(temporaryExpenseRepository).saveAll(captor.capture());
		TemporaryExpense saved = captor.getValue().get(0);

		assertThat(saved.getStatus()).isEqualTo(TemporaryExpenseStatus.NORMAL);
		assertThat(saved.getBaseCountryCode()).isEqualTo(CurrencyCode.KRW);
		assertThat(saved.getBaseCurrencyAmount()).isEqualByComparingTo("130000.00");
	}

	@Test
	@DisplayName("Gemini 429 응답이면 배치 파싱을 즉시 실패 처리한다")
	void parseBatchFiles_failsFastOnGeminiRateLimit() {
		Long accountBookId = 1L;
		File first =
				File.builder()
						.fileId(1L)
						.tempExpenseMetaId(200L)
						.fileType(File.FileType.IMAGE)
						.s3Key("temp/first.jpg")
						.build();
		File second =
				File.builder()
						.fileId(2L)
						.tempExpenseMetaId(200L)
						.fileType(File.FileType.IMAGE)
						.s3Key("temp/second.jpg")
						.build();
		TempExpenseMeta meta =
				TempExpenseMeta.builder()
						.tempExpenseMetaId(200L)
						.accountBookId(accountBookId)
						.build();

		when(accountBookRateInfoProvider.getRateInfo(accountBookId))
				.thenReturn(new AccountBookRateInfo(CurrencyCode.KRW, CurrencyCode.USD));
		when(temporaryExpenseParseClient.parse(first))
				.thenReturn(new GeminiService.GeminiParseResponse(false, List.of(), "too many requests", 429));

		assertThatThrownBy(() -> service.parseBatchFiles(meta, List.of(first, second), "task-429"))
				.isInstanceOf(RuntimeException.class);

		verify(temporaryExpenseParseClient, times(1)).parse(first);
		verify(temporaryExpenseParseClient, never()).parse(second);
		verify(progressPublisher).publishError(eq("task-429"), anyString());
		verify(progressPublisher, never()).complete(eq("task-429"));
	}
}
