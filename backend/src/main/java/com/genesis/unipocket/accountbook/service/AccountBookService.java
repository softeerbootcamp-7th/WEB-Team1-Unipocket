package com.genesis.unipocket.accountbook.service;

import com.genesis.unipocket.accountbook.facade.dto.AccountBookDto;
import com.genesis.unipocket.accountbook.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.persistence.repository.AccountBookJpaRepository;
import com.genesis.unipocket.accountbook.presentation.dto.request.CreateAccountBookReq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>가계부 서비스</b>
 * <p>
 * 가계부 서비스의 애플리케이션적 도메인(DB로 제어하기 어려운 비즈니스적 생성 범위, 값의 범위 등) 로직에 관여
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AccountBookService {
	private final AccountBookJpaRepository jpaRepository;

	@Transactional
	public AccountBookDto create(long userId, CreateAccountBookReq req) {

		AccountBookEntity newEntity =
				AccountBookEntity.create(
						userId,
						req.getTitle(),
						req.getLocalCountryCode(),
						req.getBaseCountryCode(),
						req.getStartDate(),
						req.getEndDate());

		AccountBookEntity savedEntity = jpaRepository.save(newEntity);

		return AccountBookDto.from(savedEntity);
	}
}
