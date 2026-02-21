package com.genesis.unipocket.tempexpense.query.service.port;

import java.time.Duration;

public interface TempExpenseMediaAccessService {
	String issueGetPath(String mediaKey, Duration expiration);
}
