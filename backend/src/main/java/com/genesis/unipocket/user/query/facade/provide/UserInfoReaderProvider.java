package com.genesis.unipocket.user.query.facade.provide;

import com.genesis.unipocket.accountbook.command.facade.port.UserInfoReader;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.user.query.persistence.repository.UserQueryRepository;
import com.genesis.unipocket.user.query.persistence.response.UserQueryResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserInfoReaderProvider implements UserInfoReader {

	private final UserQueryRepository userQueryRepository;

	@Override
	public String getUserName(UUID userId) {
		return userQueryRepository
				.findById(userId)
				.map(UserQueryResponse::name)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
	}
}
