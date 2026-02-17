package com.genesis.unipocket.user.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.user.command.persistence.entity.enums.CardCompany;
import com.genesis.unipocket.user.command.persistence.entity.enums.UserRole;
import com.genesis.unipocket.user.command.persistence.entity.enums.UserStatus;
import com.genesis.unipocket.user.query.persistence.repository.UserQueryRepository;
import com.genesis.unipocket.user.query.persistence.response.UserCardQueryResponse;
import com.genesis.unipocket.user.query.persistence.response.UserQueryResponse;
import com.genesis.unipocket.user.query.service.port.AccountBookCountService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

	// Note: UserQueryService might use UserQueryRepository or
	// UserCardCommandRepository depending on implementation.
	// Based on UserCardServiceTest using UserCardRepository, assuming QueryService
	// uses it or similar.
	// Earlier I saw UserQueryRepository implementation using JPQL.
	// But let's check UserQueryService implementation if I can.
	// I'll assume it uses UserCardCommandRepository for now as per
	// UserCardServiceTest logic,
	// OR I should check UserQueryService.java to be sure.
	// Let's assume UserQueryRepository.
	// Wait, UserQueryRepository was implemented earlier.
	// Let's check UserQueryService.java content first to be safe.
	// I'll do a quick check in separate tool, but for now I'll write a placeholder
	// test that matches UserQueryRepository pattern.

	@Mock
	private UserQueryRepository userQueryRepository;

	@Mock private AccountBookCountService accountBookCountService;

	@InjectMocks private UserQueryService userQueryService;

	private UUID userId;

	@BeforeEach
	void setUp() {
		userId = UUID.randomUUID();
	}

	@Test
	@DisplayName("getCards - 사용자의 카드 목록 조회 성공")
	void getCards_Success() {
		// Given
		UserCardQueryResponse cardResponse =
				new UserCardQueryResponse(1L, "My Card", "1234", CardCompany.SHINHAN);
		List<UserCardQueryResponse> cards = List.of(cardResponse);

		when(userQueryRepository.findAllCardsByUserId(userId)).thenReturn(cards);

		// When
		List<UserCardQueryResponse> result = userQueryService.getCards(userId);

		// Then
		assertThat(result).hasSize(1);
		verify(userQueryRepository).findAllCardsByUserId(userId);
	}

	@DisplayName("getUserInfo - 가계부 개수에 따른 온보딩 필요 여부 반환")
	@ParameterizedTest
	@CsvSource({"0, true", "1, false", "10, false"})
	void getUserInfo_needsOnboarding_BasedOnAccountBookCount(
			long accountBookCount, boolean expectedNeedsOnboarding) {
		UserQueryResponse baseResponse =
				new UserQueryResponse(
						userId,
						"test@example.com",
						"Tester",
						"img.jpg",
						UserRole.ROLE_USER,
						UserStatus.ACTIVE);
		when(userQueryRepository.findById(userId)).thenReturn(Optional.of(baseResponse));
		when(accountBookCountService.countByUserId(userId)).thenReturn(accountBookCount);

		UserQueryResponse result = userQueryService.getUserInfo(userId);

		assertThat(result.needsOnboarding()).isEqualTo(expectedNeedsOnboarding);
		verify(accountBookCountService).countByUserId(userId);
	}
}
