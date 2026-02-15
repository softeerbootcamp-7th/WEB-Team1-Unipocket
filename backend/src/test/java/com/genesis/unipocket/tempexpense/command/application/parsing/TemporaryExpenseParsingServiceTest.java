package com.genesis.unipocket.tempexpense.command.application.parsing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.infrastructure.gemini.GeminiService;
import com.genesis.unipocket.tempexpense.command.application.result.BatchParsingResult;
import com.genesis.unipocket.tempexpense.command.application.result.ParsingResult;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookRateInfoProvider;
import com.genesis.unipocket.tempexpense.command.facade.port.ExchangeRateProvider;
import com.genesis.unipocket.tempexpense.command.facade.port.dto.AccountBookRateInfo;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus;
import com.genesis.unipocket.tempexpense.common.infrastructure.ParsingProgressPublisher;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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
	@Mock private TempExpenseMetaRepository tempExpenseMetaRepository;
	@Mock private TemporaryExpenseRepository temporaryExpenseRepository;
	@Mock private AccountBookRateInfoProvider accountBookRateInfoProvider;
	@Mock private ExchangeRateProvider exchangeRateProvider;
	@Mock private TemporaryExpenseParseClient temporaryExpenseParseClient;
	@Mock private TemporaryExpenseFieldParser fieldParser;
	@Mock private ParsingProgressPublisher progressPublisher;

	private TemporaryExpenseParsingService service;

	@BeforeEach
	void setUp() {
		TemporaryExpensePersistenceService temporaryExpensePersistenceService =
				new TemporaryExpensePersistenceService(temporaryExpenseRepository);
		service =
				new TemporaryExpenseParsingService(
						fileRepository,
						tempExpenseMetaRepository,
						accountBookRateInfoProvider,
						exchangeRateProvider,
						fieldParser,
						temporaryExpenseParseClient,
						temporaryExpensePersistenceService,
						progressPublisher);
	}

	@Test
	@DisplayName("파싱 시 카테고리 숫자/별칭을 변환하고 환율 조회는 키별 1회만 수행")
	void parseFile_mapsCategoryAndDeduplicatesExchangeRateLookup() {
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

		when(fileRepository.findByS3Key(s3Key)).thenReturn(Optional.of(file));
		when(tempExpenseMetaRepository.findById(10L)).thenReturn(Optional.of(meta));
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

		ParsingResult result = service.parseFile(accountBookId, s3Key);

		assertThat(result.totalCount()).isEqualTo(2);
		assertThat(result.normalCount()).isEqualTo(2);
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
		assertThat(saved.get(0).getCategory()).isEqualTo(Category.FOOD);
		assertThat(saved.get(1).getCategory()).isEqualTo(Category.TRANSPORT);
		assertThat(saved.get(1).getLocalCountryCode()).isEqualTo(CurrencyCode.JPY);
		assertThat(saved.get(0).getBaseCountryCode()).isEqualTo(CurrencyCode.KRW);
		assertThat(saved.get(0).getBaseCurrencyAmount()).isEqualByComparingTo("9500.00");
		assertThat(saved.get(1).getBaseCurrencyAmount()).isEqualByComparingTo("19000.00");
	}

	@Test
	@DisplayName("거래일시가 없으면 INCOMPLETE로 저장되고 환율 조회는 수행하지 않음")
	void parseFile_incompleteItemSkipsExchangeLookup() {
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

		when(fileRepository.findByS3Key(s3Key)).thenReturn(Optional.of(file));
		when(tempExpenseMetaRepository.findById(11L)).thenReturn(Optional.of(meta));
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

		ParsingResult result = service.parseFile(accountBookId, s3Key);

		assertThat(result.incompleteCount()).isEqualTo(1);
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
	void parseFile_usesProvidedBaseAmount_skipsExchangeLookup() {
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

		when(fileRepository.findByS3Key(s3Key)).thenReturn(Optional.of(file));
		when(tempExpenseMetaRepository.findById(12L)).thenReturn(Optional.of(meta));
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

		ParsingResult result = service.parseFile(accountBookId, s3Key);

		assertThat(result.normalCount()).isEqualTo(1);

		// Verify NO interaction with ExchangeRateProvider
		verifyNoInteractions(exchangeRateProvider);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<TemporaryExpense>> captor = ArgumentCaptor.forClass(List.class);
		verify(temporaryExpenseRepository).saveAll(captor.capture());
		TemporaryExpense saved = captor.getValue().get(0);

		assertThat(saved.getLocalCurrencyAmount()).isEqualByComparingTo("100.00");
		assertThat(saved.getLocalCountryCode()).isEqualTo(CurrencyCode.USD);
		assertThat(saved.getBaseCountryCode()).isEqualTo(CurrencyCode.KRW);
		assertThat(saved.getBaseCurrencyAmount())
				.isEqualByComparingTo("135000"); // Should use the provided value
	}

	@Test
	@DisplayName("배치 파싱은 s3Key 단건 조회 대신 일괄 조회를 사용")
	void parseBatchFilesAsync_usesBulkLookups() {
		Long accountBookId = 1L;
		List<String> s3Keys = List.of("docs/a.csv", "docs/b.csv");
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
						.tempExpenseMetaId(101L)
						.fileType(File.FileType.CSV)
						.s3Key("docs/b.csv")
						.build();
		TempExpenseMeta metaA =
				TempExpenseMeta.builder()
						.tempExpenseMetaId(100L)
						.accountBookId(accountBookId)
						.build();
		TempExpenseMeta metaB =
				TempExpenseMeta.builder()
						.tempExpenseMetaId(101L)
						.accountBookId(accountBookId)
						.build();

		when(accountBookRateInfoProvider.getRateInfo(accountBookId))
				.thenReturn(new AccountBookRateInfo(CurrencyCode.KRW, CurrencyCode.JPY));
		when(fileRepository.findByS3KeyIn(s3Keys)).thenReturn(List.of(fileA, fileB));
		when(tempExpenseMetaRepository.findAllById(any())).thenReturn(List.of(metaA, metaB));
		when(temporaryExpenseParseClient.parse(any(File.class)))
				.thenReturn(new GeminiService.GeminiParseResponse(true, List.of(), null));
		when(temporaryExpenseRepository.saveAll(anyList()))
				.thenAnswer(invocation -> invocation.getArgument(0));

		CompletableFuture<BatchParsingResult> future =
				service.parseBatchFilesAsync(accountBookId, s3Keys, "task-1");
		BatchParsingResult result = future.join();

		assertThat(result.totalParsed()).isEqualTo(0);
		verify(fileRepository).findByS3KeyIn(s3Keys);
		verify(fileRepository, never()).findByS3Key(anyString());
		verify(tempExpenseMetaRepository, times(1)).findAllById(any());
		verify(progressPublisher, times(1)).complete(eq("task-1"), any(BatchParsingResult.class));
	}
}
