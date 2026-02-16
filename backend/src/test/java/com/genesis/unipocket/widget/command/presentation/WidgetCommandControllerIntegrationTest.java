package com.genesis.unipocket.widget.command.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.genesis.unipocket.TestcontainersConfiguration;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookCreateArgs;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.command.persistence.repository.AccountBookCommandRepository;
import com.genesis.unipocket.auth.support.JwtTestHelper;
import com.genesis.unipocket.global.common.enums.CountryCode;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test-it")
@Import(TestcontainersConfiguration.class)
@Transactional
@Tag("integration")
class WidgetCommandControllerIntegrationTest {

	@Autowired private MockMvc mockMvc;
	@Autowired private JwtTestHelper jwtTestHelper;
	@Autowired private UserCommandRepository userRepository;
	@Autowired private AccountBookCommandRepository accountBookRepository;

	private UUID userId;
	private Long accountBookId;

	@BeforeEach
	void setUp() {
		UserEntity user =
				userRepository.save(
						UserEntity.builder()
								.email("widget-command@unipocket.com")
								.name("widget-command")
								.mainBucketId(1L)
								.build());
		userId = user.getId();

		AccountBookEntity accountBook =
				accountBookRepository.save(
						AccountBookEntity.create(
								new AccountBookCreateArgs(
										user,
										"Widget Command Book",
										CountryCode.KR,
										CountryCode.KR,
										1,
										null,
										LocalDate.of(2026, 1, 1),
										LocalDate.of(2026, 12, 31))));
		accountBookId = accountBook.getId();
	}

	@Test
	@DisplayName("가계부 위젯 등록/수정 API는 500 없이 정상 응답한다")
	void updateAccountBookWidgets_success() throws Exception {
		String body =
				"""
			[
			{"order":0,"widgetType":"BUDGET","currencyType":"BASE","period":"ALL"},
			{"order":1,"widgetType":"PERIOD","currencyType":"BASE","period":"MONTHLY"}
			]
			""";

		mockMvc.perform(
						put("/account-books/{accountBookId}/widgets", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.contentType(MediaType.APPLICATION_JSON)
								.content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].widgetType").value("BUDGET"))
				.andExpect(jsonPath("$[1].widgetType").value("PERIOD"));
	}
}
