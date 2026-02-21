package com.genesis.unipocket.auth.command.facade.port;

import java.util.UUID;

public interface UserExistenceChecker {

	boolean existsById(UUID userId);
}
