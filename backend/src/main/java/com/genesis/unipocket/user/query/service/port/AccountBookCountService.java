package com.genesis.unipocket.user.query.service.port;

import java.util.UUID;

public interface AccountBookCountService {
	long countByUserId(UUID userId);
}
