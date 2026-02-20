package com.genesis.unipocket.accountbook.command.application.port;

import com.genesis.unipocket.accountbook.command.application.port.dto.AccountBookUserInfo;
import java.util.UUID;

public interface AccountBookUserReader {

	AccountBookUserInfo getUser(UUID userId);

	void updateMainAccountBook(UUID userId, Long accountBookId);
}
