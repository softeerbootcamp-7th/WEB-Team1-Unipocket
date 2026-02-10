package com.genesis.unipocket.user.command.facade.port;

import com.genesis.unipocket.user.command.application.result.LoginOrRegisterResult;
import java.util.UUID;

public interface TokenIssuePort {

	LoginOrRegisterResult issueTokens(UUID userId);
}
