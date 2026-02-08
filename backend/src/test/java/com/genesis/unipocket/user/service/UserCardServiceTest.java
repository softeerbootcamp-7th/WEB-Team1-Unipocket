package com.genesis.unipocket.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.user.dto.request.UserCardRequest;
import com.genesis.unipocket.user.dto.response.UserCardResponse;
import com.genesis.unipocket.user.persistence.entity.UserCardEntity;
import com.genesis.unipocket.user.persistence.entity.UserEntity;
import com.genesis.unipocket.user.persistence.entity.enums.CardCompany;
import com.genesis.unipocket.user.persistence.repository.UserCardRepository;
import com.genesis.unipocket.user.persistence.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserCardServiceTest {

	@Mock private UserCardRepository userCardRepository;

	@Mock private UserRepository userRepository;

	@InjectMocks private UserCardService userCardService;

	@Mock private UserEntity user;

	@Mock private UserEntity otherUser;

	@Mock private UserCardEntity userCard;

	private UUID userId;
	private UUID otherUserId;
	private Long cardId;

	@BeforeEach
	void setUp() {
		userId = UUID.randomUUID();
		otherUserId = UUID.randomUUID();
		cardId = 1L;

		// Mock UserEntity - use lenient() to avoid UnnecessaryStubbingException
		lenient().when(user.getId()).thenReturn(userId);

		lenient().when(otherUser.getId()).thenReturn(otherUserId);

		// Mock UserCardEntity
		lenient().when(userCard.getUser()).thenReturn(user);
		lenient().when(userCard.getUserCardId()).thenReturn(cardId);
	}

	@Test
	@DisplayName("createCard - 카드 생성 성공")
	void createCard_Success() {
		// Given
		UserCardRequest request = new UserCardRequest("My Card", "3456", CardCompany.SHINHAN);

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userCardRepository.save(any(UserCardEntity.class))).thenReturn(userCard);

		// When
		Long result = userCardService.createCard(userId, request);

		// Then
		assertThat(result).isEqualTo(cardId);
		verify(userRepository).findById(userId);
		verify(userCardRepository).save(any(UserCardEntity.class));
	}

	@Test
	@DisplayName("createCard - 사용자를 찾을 수 없음")
	void createCard_UserNotFound() {
		// Given
		UserCardRequest request = new UserCardRequest("My Card", "3456", CardCompany.SHINHAN);

		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> userCardService.createCard(userId, request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", ErrorCode.USER_NOT_FOUND);

		verify(userCardRepository, never()).save(any());
	}

	@Test
	@DisplayName("getCards - 사용자의 카드 목록 조회 성공")
	void getCards_Success() {
		// Given
		List<UserCardEntity> cards = List.of(userCard);
		when(userCardRepository.findAllByUser_Id(userId)).thenReturn(cards);

		// When
		List<UserCardResponse> result = userCardService.getCards(userId);

		// Then
		assertThat(result).hasSize(1);
		verify(userCardRepository).findAllByUser_Id(userId);
	}

	@Test
	@DisplayName("deleteCard - 소유자가 카드 삭제 성공")
	void deleteCard_Success() {
		// Given
		when(userCardRepository.findById(cardId)).thenReturn(Optional.of(userCard));

		// When
		userCardService.deleteCard(cardId, userId);

		// Then
		verify(userCardRepository).findById(cardId);
		verify(userCardRepository).delete(userCard);
	}

	@Test
	@DisplayName("deleteCard - 카드를 찾을 수 없음")
	void deleteCard_CardNotFound() {
		// Given
		when(userCardRepository.findById(cardId)).thenReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> userCardService.deleteCard(cardId, userId))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", ErrorCode.RESOURCE_NOT_FOUND);

		verify(userCardRepository, never()).delete(any());
	}

	@Test
	@DisplayName("deleteCard - 비소유자가 카드 삭제 시도 (403 Forbidden)")
	void deleteCard_Forbidden() {
		// Given
		when(userCardRepository.findById(cardId)).thenReturn(Optional.of(userCard));

		// When & Then
		assertThatThrownBy(() -> userCardService.deleteCard(cardId, otherUserId))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", ErrorCode.FORBIDDEN);

		verify(userCardRepository).findById(cardId);
		verify(userCardRepository, never()).delete(any());
	}
}
