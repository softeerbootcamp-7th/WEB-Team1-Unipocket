package com.genesis.unipocket.accountbook.command.facade.port;

import java.util.UUID;

public interface UserInfoReader {

	String getUserName(UUID userId);
}
