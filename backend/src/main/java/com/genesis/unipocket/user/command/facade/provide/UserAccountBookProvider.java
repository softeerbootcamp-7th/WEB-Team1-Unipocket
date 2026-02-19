package com.genesis.unipocket.user.command.facade.provide;

import com.genesis.unipocket.accountbook.command.facade.port.UserMainAccountBookService;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAccountBookProvider implements UserMainAccountBookService {

	private final UserCommandRepository userCommandRepository;

	@Override
	public void updateMainAccountBook(UUID userId, Long accountBookId) {
		var user =
				userCommandRepository
						.findById(userId)
						.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
		user.updateMainBucketId(accountBookId);
	}
}
