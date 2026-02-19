package com.genesis.unipocket.tempexpense.command.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.genesis.unipocket.TestcontainersConfiguration;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookCreateArgs;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.command.persistence.repository.AccountBookCommandRepository;
import com.genesis.unipocket.auth.support.JwtTestHelper;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test-it")
@Import(TestcontainersConfiguration.class)
@TestPropertySource(
		properties = {
			"tempexpense.rate-limit.parse.max-requests-per-minute=1",
		})
@Transactional
@Tag("integration")
class TemporaryExpenseRateLimitIntegrationTest {

	@Autowired private MockMvc mockMvc;
	@Autowired private JwtTestHelper jwtTestHelper;
	@Autowired private AccountBookCommandRepository accountBookRepository;
	@Autowired private UserCommandRepository userRepository;
	@Autowired private TempExpenseMetaRepository tempExpenseMetaRepository;
	@Autowired private StringRedisTemplate redisTemplate;

	private Long accountBookId;
	private Long tempExpenseMetaId;
	private UUID userId;

	@BeforeEach
	void setUp() {
		flushRedis();

		UserEntity user =
				userRepository.save(
						UserEntity.builder()
								.email("rate-limit-user@unipocket.com")
								.name("rate-limit-user")
								.mainBucketId(1L)
								.build());
		userId = user.getId();

		AccountBookCreateArgs args =
				new AccountBookCreateArgs(
						user,
						"Rate Limit Account Book",
						CountryCode.KR,
						CountryCode.KR,
						1,
						null,
						LocalDate.of(2026, 1, 1),
						LocalDate.of(2026, 12, 31));
		AccountBookEntity accountBook = accountBookRepository.save(AccountBookEntity.create(args));
		accountBookId = accountBook.getId();

		TempExpenseMeta meta =
				tempExpenseMetaRepository.save(
						TempExpenseMeta.builder().accountBookId(accountBookId).build());
		tempExpenseMetaId = meta.getTempExpenseMetaId();
	}

	private void flushRedis() {
		var connectionFactory = redisTemplate.getConnectionFactory();
		if (connectionFactory == null) {
			return;
		}
		try (RedisConnection connection = connectionFactory.getConnection()) {
			connection.serverCommands().flushDb();
		}
	}

	@Test
	@DisplayName("파싱 시작 API는 사용자 기준 분당 제한 초과 시 429 + Retry-After를 반환한다")
	void parseRateLimitExceeded() throws Exception {
		String body =
				"""
			{
			"tempExpenseMetaId": %d,
			"s3Keys": []
			}
			"""
						.formatted(tempExpenseMetaId);

		mockMvc.perform(
						post(
										"/account-books/{accountBookId}/temporary-expenses/parse",
										accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.contentType(MediaType.APPLICATION_JSON)
								.content(body))
				.andExpect(status().isBadRequest());

		mockMvc.perform(
						post(
										"/account-books/{accountBookId}/temporary-expenses/parse",
										accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.contentType(MediaType.APPLICATION_JSON)
								.content(body))
				.andExpect(status().isTooManyRequests())
				.andExpect(header().exists("Retry-After"))
				.andExpect(
						jsonPath("$.code")
								.value(ErrorCode.TEMP_EXPENSE_RATE_LIMIT_EXCEEDED.getCode()));
	}
}
