package com.genesis.unipocket.accountbook.query.facade.provide;

import com.genesis.unipocket.accountbook.query.persistence.repository.AccountBookQueryRepository;
import com.genesis.unipocket.user.query.application.port.AccountBookCountService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountBookCountProvider implements AccountBookCountService {

	private final AccountBookQueryRepository accountBookQueryRepository;

	@Override
	public long countByUserId(UUID userId) {
		return accountBookQueryRepository.countByUserId(userId);
	}
}
