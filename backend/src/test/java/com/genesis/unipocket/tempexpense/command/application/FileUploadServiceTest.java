package com.genesis.unipocket.tempexpense.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.media.command.application.result.PresignedUrlResult;
import com.genesis.unipocket.tempexpense.command.application.result.FileUploadResult;
import com.genesis.unipocket.tempexpense.command.facade.port.TempExpenseMediaAccessService;
import com.genesis.unipocket.tempexpense.command.facade.provide.TemporaryExpenseScopeValidationProvider;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.tempexpense.command.presentation.request.PresignedUrlRequest.UploadType;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class FileUploadServiceTest {

	@Mock private TempExpenseMetaRepository tempExpenseMetaRepository;
	@Mock private FileRepository fileRepository;
	@Mock private TemporaryExpenseRepository temporaryExpenseRepository;
	@Mock private TempExpenseMediaAccessService tempExpenseMediaAccessService;
	@Mock private TemporaryExpenseScopeValidationProvider temporaryExpenseScopeValidationProvider;

	private FileUploadService fileUploadService;

	@BeforeEach
	void setUp() {
		fileUploadService =
				new FileUploadService(
						tempExpenseMetaRepository,
						fileRepository,
						temporaryExpenseRepository,
						tempExpenseMediaAccessService,
						temporaryExpenseScopeValidationProvider);
		ReflectionTestUtils.setField(fileUploadService, "presignedPutExpirationSeconds", 300);
	}

	@Test
	@DisplayName("업로드 URL 발급 시 입력된 fileName을 저장하고 응답으로 반환한다")
	void createPresignedUrl_returnsProvidedFileName() {
		Long accountBookId = 1L;
		Long metaId = 10L;
		TempExpenseMeta meta =
				TempExpenseMeta.builder()
						.tempExpenseMetaId(metaId)
						.accountBookId(accountBookId)
						.build();

		when(temporaryExpenseScopeValidationProvider.validateMetaScope(accountBookId, metaId))
				.thenReturn(meta);
		when(fileRepository.findByTempExpenseMetaId(metaId)).thenReturn(List.of());
		when(tempExpenseMediaAccessService.issueUploadPath(accountBookId, "image/png"))
				.thenReturn(new PresignedUrlResult("put-url", "backend/temp-expenses/1/abc.png"));
		when(fileRepository.save(any(File.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		FileUploadResult result =
				fileUploadService.createPresignedUrl(
						accountBookId, "receipt-abc.png", "image/png", UploadType.IMAGE, metaId);

		assertThat(result.fileName()).isEqualTo("receipt-abc.png");

		ArgumentCaptor<File> captor = ArgumentCaptor.forClass(File.class);
		verify(fileRepository).save(captor.capture());
		assertThat(captor.getValue().getFileName()).isEqualTo("receipt-abc.png");
	}

	@Test
	@DisplayName("업로드 URL 발급 시 fileName이 비어있으면 s3Key 기반 unknown 이름으로 응답한다")
	void createPresignedUrl_returnsUnknownFileNameWhenInputMissing() {
		Long accountBookId = 1L;
		Long metaId = 11L;
		TempExpenseMeta meta =
				TempExpenseMeta.builder()
						.tempExpenseMetaId(metaId)
						.accountBookId(accountBookId)
						.build();

		when(temporaryExpenseScopeValidationProvider.validateMetaScope(accountBookId, metaId))
				.thenReturn(meta);
		when(fileRepository.findByTempExpenseMetaId(metaId)).thenReturn(List.of());
		when(tempExpenseMediaAccessService.issueUploadPath(accountBookId, "image/png"))
				.thenReturn(new PresignedUrlResult("put-url", "backend/temp-expenses/1/abc.png"));
		when(fileRepository.save(any(File.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		FileUploadResult result =
				fileUploadService.createPresignedUrl(
						accountBookId, null, "image/png", UploadType.IMAGE, metaId);

		assertThat(result.fileName()).isEqualTo("unknown_file.png");
	}

	@Test
	@DisplayName("문서 업로드는 xls mimeType을 허용한다")
	void createPresignedUrl_docsXls_acceptsMimeType() {
		Long accountBookId = 2L;
		Long metaId = 20L;
		TempExpenseMeta meta =
				TempExpenseMeta.builder()
						.tempExpenseMetaId(metaId)
						.accountBookId(accountBookId)
						.build();

		when(temporaryExpenseScopeValidationProvider.validateMetaScope(accountBookId, metaId))
				.thenReturn(meta);
		when(fileRepository.findByTempExpenseMetaId(metaId)).thenReturn(List.of());
		when(tempExpenseMediaAccessService.issueUploadPath(
						accountBookId, "application/vnd.ms-excel"))
				.thenReturn(
						new PresignedUrlResult("put-url", "backend/temp-expenses/2/statement.xls"));
		when(fileRepository.save(any(File.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		fileUploadService.createPresignedUrl(
				accountBookId,
				"statement.xls",
				"application/vnd.ms-excel",
				UploadType.DOCS,
				metaId);

		ArgumentCaptor<File> captor = ArgumentCaptor.forClass(File.class);
		verify(fileRepository).save(captor.capture());
		assertThat(captor.getValue().getFileType()).isEqualTo(File.FileType.EXCEL);
	}

	@Test
	@DisplayName("문서 업로드는 generic mimeType이면 거부한다")
	void createPresignedUrl_docsRejectsGenericMimeType() {
		Long accountBookId = 3L;
		Long metaId = 30L;
		TempExpenseMeta meta =
				TempExpenseMeta.builder()
						.tempExpenseMetaId(metaId)
						.accountBookId(accountBookId)
						.build();

		when(temporaryExpenseScopeValidationProvider.validateMetaScope(accountBookId, metaId))
				.thenReturn(meta);

		assertThatThrownBy(
						() ->
								fileUploadService.createPresignedUrl(
										accountBookId,
										"statement.xlsx",
										"application/octet-stream",
										UploadType.DOCS,
										metaId))
				.isInstanceOf(BusinessException.class)
				.extracting("code")
				.isEqualTo(ErrorCode.UNSUPPORTED_MEDIA_TYPE);
	}
}
