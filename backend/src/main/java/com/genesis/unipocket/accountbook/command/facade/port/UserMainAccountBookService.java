package com.genesis.unipocket.accountbook.command.facade.port;

import java.util.UUID;

public interface UserMainAccountBookService {
	void updateMainAccountBook(UUID userId, Long accountBookId);
}
