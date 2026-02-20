package com.genesis.unipocket.user.command.facade.provide;

import com.genesis.unipocket.accountbook.command.facade.port.UserInfoReader;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserInfoReaderProvider implements UserInfoReader {

	private final UserCommandRepository userCommandRepository;

	@Override
	public String getUserName(UUID userId) {
		return userCommandRepository
				.findById(userId)
				.map(user -> user.getName())
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
	}
}
