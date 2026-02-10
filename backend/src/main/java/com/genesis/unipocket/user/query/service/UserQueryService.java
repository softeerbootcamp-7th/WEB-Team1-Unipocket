package com.genesis.unipocket.user.query.service;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.user.command.persistence.entity.enums.CardCompany;
import com.genesis.unipocket.user.query.persistence.repository.UserQueryRepository;
import com.genesis.unipocket.user.query.persistence.response.UserCardQueryResponse;
import com.genesis.unipocket.user.query.persistence.response.UserQueryResponse;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

    private final UserQueryRepository userQueryRepository;

    public UserQueryResponse getUserInfo(UUID userId) {
        return userQueryRepository
                .findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    public List<UserCardQueryResponse> getCards(UUID userId) {
        return userQueryRepository.findAllCardsByUserId(userId);
    }

    public List<CardCompany> getCardCompanies() {
        return Arrays.asList(CardCompany.values());
    }
}
