package com.genesis.unipocket.user.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.user.command.application.command.CreateCardCommand;
import com.genesis.unipocket.user.command.application.command.DeleteCardCommand;
import com.genesis.unipocket.user.command.persistence.entity.UserCardEntity;
import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import com.genesis.unipocket.user.command.persistence.entity.enums.CardCompany;
import com.genesis.unipocket.user.command.persistence.repository.UserCardCommandRepository;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import com.genesis.unipocket.user.command.presentation.request.UserCardRequest;
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
class UserCommandServiceTest {

    @Mock
    private UserCardCommandRepository userCardRepository;
    @Mock
    private UserCommandRepository userRepository;

    @InjectMocks
    private UserCommandService userCommandService;

    @Mock
    private UserEntity user;
    @Mock
    private UserCardEntity userCard;

    private UUID userId;
    private UUID otherUserId;
    private Long cardId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        cardId = 1L;
    }

    @Test
    @DisplayName("createCard - 카드 생성 성공")
    void createCard_Success() {
        // Given
        UserCardRequest request = new UserCardRequest("My Card", "3456", CardCompany.SHINHAN);
        CreateCardCommand command = CreateCardCommand.of(userId, request);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userCardRepository.save(any(UserCardEntity.class))).thenReturn(userCard);
        when(userCard.getUserCardId()).thenReturn(cardId);

        // When
        Long result = userCommandService.createCard(command);

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
        CreateCardCommand command = CreateCardCommand.of(userId, request);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userCommandService.createCard(command))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.USER_NOT_FOUND);

        verify(userCardRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteCard - 소유자가 카드 삭제 성공")
    void deleteCard_Success() {
        // Given
        DeleteCardCommand command = DeleteCardCommand.of(cardId, userId);
        when(userCardRepository.findById(cardId)).thenReturn(Optional.of(userCard));
        when(userCard.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);

        // When
        userCommandService.deleteCard(command);

        // Then
        verify(userCardRepository).findById(cardId);
        verify(userCardRepository).delete(userCard);
    }

    @Test
    @DisplayName("deleteCard - 카드를 찾을 수 없음")
    void deleteCard_CardNotFound() {
        // Given
        DeleteCardCommand command = DeleteCardCommand.of(cardId, userId);
        when(userCardRepository.findById(cardId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userCommandService.deleteCard(command))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.CARD_NOT_FOUND); // Assuming CARD_NOT_FOUND or
                                                                                // RESOURCE_NOT_FOUND

        verify(userCardRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteCard - 비소유자가 카드 삭제 시도 (403 Forbidden)")
    void deleteCard_Forbidden() {
        // Given
        DeleteCardCommand command = DeleteCardCommand.of(cardId, otherUserId);
        when(userCardRepository.findById(cardId)).thenReturn(Optional.of(userCard));
        when(userCard.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);

        // When & Then
        assertThatThrownBy(() -> userCommandService.deleteCard(command))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.CARD_NOT_OWNED); // Assuming CARD_NOT_OWNED or FORBIDDEN

        verify(userCardRepository).findById(cardId);
        verify(userCardRepository, never()).delete(any());
    }
}
