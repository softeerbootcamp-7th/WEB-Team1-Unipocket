package com.genesis.unipocket.tempexpense.command.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genesis.unipocket.auth.command.application.JwtProvider;
import com.genesis.unipocket.auth.command.application.TokenBlacklistService;
import com.genesis.unipocket.auth.common.constant.AuthCookieConstants;
import com.genesis.unipocket.tempexpense.command.application.result.ConfirmStartResult;
import com.genesis.unipocket.tempexpense.command.application.result.FileUploadResult;
import com.genesis.unipocket.tempexpense.command.application.result.ParseStartResult;
import com.genesis.unipocket.tempexpense.command.facade.TemporaryExpenseCommandFacade;
import com.genesis.unipocket.tempexpense.command.presentation.request.BatchParseRequest;
import com.genesis.unipocket.tempexpense.command.presentation.request.PresignedUrlRequest;
import com.genesis.unipocket.tempexpense.command.presentation.request.PresignedUrlRequest.UploadType;
import com.genesis.unipocket.tempexpense.command.presentation.response.TemporaryExpenseMetaBulkUpdateResponse;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TemporaryExpenseCommandController.class)
class TemporaryExpenseCommandControllerTest {

	@Autowired private MockMvc mockMvc;
	@Autowired private ObjectMapper objectMapper;

	@MockitoBean private TemporaryExpenseCommandFacade temporaryExpenseCommandFacade;
	@MockitoBean private JwtProvider jwtProvider;
	@MockitoBean private TokenBlacklistService tokenBlacklistService;
	@MockitoBean private UserCommandRepository userRepository;

	@SuppressWarnings("unused")
	@MockitoBean
	private JpaMetamodelMappingContext jpaMetamodelMappingContext;

	@Test
	@DisplayName("임시지출 업로드 URL 발급 성공")
	void createPresignedUrl_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 1L;
		PresignedUrlRequest request =
				new PresignedUrlRequest("receipt.jpg", "image/jpeg", UploadType.IMAGE, null);
		FileUploadResult result =
				new FileUploadResult(
						100L, "https://s3.example.com/upload", "temp/key.jpg", "receipt.jpg", 300);

		mockAuthentication(accessToken, userId);
		given(
						temporaryExpenseCommandFacade.createPresignedUrl(
								eq(accountBookId),
								eq("receipt.jpg"),
								eq("image/jpeg"),
								eq(UploadType.IMAGE),
								any(),
								eq(userId)))
				.willReturn(result);

		mockMvc.perform(
						post(
										"/account-books/{accountBookId}/temporary-expenses/uploads/presigned-url",
										accountBookId)
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tempExpenseMetaId").value(100))
				.andExpect(jsonPath("$.presignedUrl").value("https://s3.example.com/upload"))
				.andExpect(jsonPath("$.s3Key").value("temp/key.jpg"));
	}

	@Test
	@DisplayName("임시지출 확정 성공")
	void confirm_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 1L;
		Long tempExpenseMetaId = 100L;
		ConfirmStartResult result = new ConfirmStartResult("task-001", 5);

		mockAuthentication(accessToken, userId);
		given(temporaryExpenseCommandFacade.confirm(accountBookId, tempExpenseMetaId, userId))
				.willReturn(result);

		mockMvc.perform(
						post(
										"/account-books/{accountBookId}/temporary-expense-metas/{tempExpenseMetaId}/confirm",
										accountBookId,
										tempExpenseMetaId)
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.taskId").value("task-001"))
				.andExpect(jsonPath("$.totalExpenses").value(5));
	}

	@Test
	@DisplayName("임시지출 파싱 시작 성공")
	void parse_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 1L;
		BatchParseRequest request =
				new BatchParseRequest(100L, List.of("s3key1.jpg", "s3key2.jpg"));
		ParseStartResult result = new ParseStartResult("task-002", 2);

		mockAuthentication(accessToken, userId);
		given(
						temporaryExpenseCommandFacade.startParseAsync(
								eq(accountBookId), eq(100L), any(), eq(userId)))
				.willReturn(result);

		mockMvc.perform(
						post(
										"/account-books/{accountBookId}/temporary-expenses/parse",
										accountBookId)
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isAccepted())
				.andExpect(jsonPath("$.taskId").value("task-002"))
				.andExpect(jsonPath("$.totalFiles").value(2))
				.andExpect(jsonPath("$.statusUrl").exists());
	}

	@Test
	@DisplayName("임시지출 일괄 수정 성공")
	void updateTemporaryExpensesByFile_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 1L;
		Long tempExpenseMetaId = 100L;
		Long fileId = 50L;

		TemporaryExpenseMetaBulkUpdateResponse response =
				new TemporaryExpenseMetaBulkUpdateResponse(3, 3, 0, List.of());

		mockAuthentication(accessToken, userId);
		given(
						temporaryExpenseCommandFacade.updateTemporaryExpensesByFile(
								eq(accountBookId),
								eq(tempExpenseMetaId),
								eq(fileId),
								any(),
								eq(userId)))
				.willReturn(response);

		String body = "{\"items\":[{\"tempExpenseId\":1}]}";
		mockMvc.perform(
						patch(
										"/account-books/{accountBookId}/temporary-expense-metas/{tempExpenseMetaId}/files/{fileId}/temporary-expenses",
										accountBookId,
										tempExpenseMetaId,
										fileId)
								.contentType(MediaType.APPLICATION_JSON)
								.content(body)
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("임시지출 메타 삭제 성공")
	void deleteMeta_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 1L;
		Long tempExpenseMetaId = 100L;

		mockAuthentication(accessToken, userId);
		doNothing()
				.when(temporaryExpenseCommandFacade)
				.deleteMeta(accountBookId, tempExpenseMetaId, userId);

		mockMvc.perform(
						delete(
										"/account-books/{accountBookId}/temporary-expense-metas/{tempExpenseMetaId}",
										accountBookId,
										tempExpenseMetaId)
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("임시지출 단건 삭제 성공")
	void deleteTemporaryExpenseByFile_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 1L;
		Long tempExpenseMetaId = 100L;
		Long fileId = 50L;
		Long tempExpenseId = 200L;

		mockAuthentication(accessToken, userId);
		doNothing()
				.when(temporaryExpenseCommandFacade)
				.deleteTemporaryExpenseByFile(
						accountBookId, tempExpenseMetaId, fileId, tempExpenseId, userId);

		mockMvc.perform(
						delete(
										"/account-books/{accountBookId}/temporary-expense-metas/{tempExpenseMetaId}/files/{fileId}/temporary-expenses/{tempExpenseId}",
										accountBookId,
										tempExpenseMetaId,
										fileId,
										tempExpenseId)
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isNoContent());
	}

	private void mockAuthentication(String accessToken, UUID userId) {
		given(jwtProvider.validateToken(accessToken)).willReturn(true);
		given(jwtProvider.getJti(accessToken)).willReturn("jti");
		given(tokenBlacklistService.isBlacklisted("jti")).willReturn(false);
		given(jwtProvider.getUserId(accessToken)).willReturn(userId);
	}
}
