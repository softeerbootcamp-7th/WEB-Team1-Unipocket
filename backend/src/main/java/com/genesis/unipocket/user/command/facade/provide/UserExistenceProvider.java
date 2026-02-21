package com.genesis.unipocket.user.command.facade.provide;

import com.genesis.unipocket.auth.command.facade.port.UserExistenceChecker;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserExistenceProvider implements UserExistenceChecker {

	private final UserCommandRepository userCommandRepository;

	@Override
	public boolean existsById(UUID userId) {
		return userCommandRepository.existsById(userId);
	}
}
