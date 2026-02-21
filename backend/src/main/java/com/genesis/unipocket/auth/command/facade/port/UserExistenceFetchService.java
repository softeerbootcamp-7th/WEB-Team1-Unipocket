package com.genesis.unipocket.auth.command.facade.port;

import java.util.UUID;

public interface UserExistenceFetchService {

	boolean existsById(UUID userId);
}
