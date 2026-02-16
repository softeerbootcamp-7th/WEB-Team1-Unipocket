package com.genesis.unipocket.accountbook.command.facade.provide;

import com.genesis.unipocket.accountbook.command.persistence.repository.AccountBookCommandRepository;
import com.genesis.unipocket.user.query.service.port.AccountBookCountService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountBookCountProvider implements AccountBookCountService {

	private final AccountBookCommandRepository accountBookCommandRepository;

	@Override
	public long countByUserId(UUID userId) {
		return accountBookCommandRepository.countByUser_Id(userId);
	}
}
