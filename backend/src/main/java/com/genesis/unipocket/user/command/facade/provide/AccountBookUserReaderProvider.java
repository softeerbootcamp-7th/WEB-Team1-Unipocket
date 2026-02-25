package com.genesis.unipocket.user.command.facade.provide;

import com.genesis.unipocket.accountbook.command.application.port.AccountBookUserReader;
import com.genesis.unipocket.accountbook.command.application.port.dto.AccountBookUserInfo;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AccountBookUserReaderProvider implements AccountBookUserReader {

	private final UserCommandRepository userCommandRepository;

	@Override
	@Transactional(readOnly = true)
	public AccountBookUserInfo getUser(UUID userId) {
		return userCommandRepository
				.findById(userId)
				.map(user -> new AccountBookUserInfo(user.getId(), user.hasMainBucket()))
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
	}

	@Override
	@Transactional
	public void updateMainAccountBook(UUID userId, Long accountBookId) {
		userCommandRepository
				.findById(userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
				.updateMainBucketId(accountBookId);
	}
}
