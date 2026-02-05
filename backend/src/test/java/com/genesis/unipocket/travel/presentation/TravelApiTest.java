package com.genesis.unipocket.travel.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genesis.unipocket.accountbook.command.persistence.dto.AccountBookCreateArgs;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.command.persistence.repository.AccountBookRepository;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.travel.domain.WidgetType;
import com.genesis.unipocket.travel.dto.TravelRequest;
import com.genesis.unipocket.travel.dto.WidgetDto;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local") // Use H2
@Transactional
class TravelApiTest {

	@Autowired private MockMvc mockMvc;
	@Autowired private ObjectMapper objectMapper;
	@Autowired private AccountBookRepository accountBookRepository;

	private Long accountBookId;

	@BeforeEach
	void setUp() {
		// Create an AccountBook to link with
		AccountBookCreateArgs args =
				new AccountBookCreateArgs(
						"user1",
						"My Trip Account",
						CountryCode.KR,
						CountryCode.KR,
						LocalDate.of(2024, 1, 1),
						LocalDate.of(2024, 12, 31));
		AccountBookEntity accountBook = AccountBookEntity.create(args);
		accountBook = accountBookRepository.save(accountBook);
		accountBookId = accountBook.getId();
	}

	@Test
	@DisplayName("여행 폴더 생성 및 조회 테스트")
	void createAndGetTravel() throws Exception {
		// 1. Create Travel
		TravelRequest request =
				new TravelRequest(
						accountBookId,
						"Tokyo Trip",
						LocalDate.of(2024, 5, 1),
						LocalDate.of(2024, 5, 5),
						"img_tokyo");

		String jsonRequest = objectMapper.writeValueAsString(request);

		String location =
				mockMvc.perform(
								post("/api/travels")
										.contentType(MediaType.APPLICATION_JSON)
										.content(jsonRequest))
						.andExpect(status().isCreated())
						.andReturn()
						.getResponse()
						.getHeader("Location");

		String travelIdStr = location.substring(location.lastIndexOf("/") + 1);
		Long travelId = Long.parseLong(travelIdStr);

		// 2. Get Details
		mockMvc.perform(get("/api/travels/" + travelId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.travelId").value(travelId))
				.andExpect(jsonPath("$.accountBookId").value(accountBookId))
				.andExpect(jsonPath("$.travelPlaceName").value("Tokyo Trip"));

		// 3. Update Widgets
		List<WidgetDto> widgets =
				List.of(
						new WidgetDto(WidgetType.SUMMARY_CARD, 1),
						new WidgetDto(WidgetType.GRAPH_DAILY, 2));

		mockMvc.perform(
						put("/api/travels/" + travelId + "/widgets")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(widgets)))
				.andExpect(status().isOk());

		// 4. Verify Widgets in Detail
		mockMvc.perform(get("/api/travels/" + travelId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.widgets[0].type").value("SUMMARY_CARD"))
				.andExpect(jsonPath("$.widgets[1].type").value("GRAPH_DAILY"));
	}
}
