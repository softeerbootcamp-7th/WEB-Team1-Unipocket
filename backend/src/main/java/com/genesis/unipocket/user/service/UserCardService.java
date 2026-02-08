package com.genesis.unipocket.user.service;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.user.dto.request.UserCardRequest;
import com.genesis.unipocket.user.dto.response.UserCardResponse;
import com.genesis.unipocket.user.persistence.entity.UserCardEntity;
import com.genesis.unipocket.user.persistence.entity.UserEntity;
import com.genesis.unipocket.user.persistence.repository.UserCardRepository;
import com.genesis.unipocket.user.persistence.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserCardService {

	private final UserCardRepository userCardRepository;
	private final UserRepository userRepository;

	/**
	 * 카드 등록
	 */
	@Transactional
	public Long createCard(UUID userId, UserCardRequest request) {
		UserEntity user =
				userRepository
						.findById(userId)
						.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		UserCardEntity userCard =
				UserCardEntity.builder()
						.user(user)
						.nickName(request.nickName())
						.cardNumber(request.cardNumber())
						.cardCompany(request.cardCompany())
						.build();

		return userCardRepository.save(userCard).getUserCardId();
	}

	/**
	 * 사용자별 카드 목록 조회
	 */
	public List<UserCardResponse> getCards(UUID userId) {
		return userCardRepository.findAllByUser_Id(userId).stream()
				.map(UserCardResponse::from)
				.collect(Collectors.toList());
	}

	/**
	 * 카드 삭제 (소유권 검증 포함)
	 */
	@Transactional
	public void deleteCard(Long cardId, UUID userId) {
		UserCardEntity userCard =
				userCardRepository
						.findById(cardId)
						.orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

		// 소유권 검증: 카드의 소유자가 요청한 사용자와 일치하는지 확인
		if (!userCard.getUser().getId().equals(userId)) {
			throw new BusinessException(ErrorCode.FORBIDDEN);
		}

		userCardRepository.delete(userCard);
	}
}
