package com.genesis.unipocket.tempexpense.query.application.port;

import java.time.Duration;

public interface TempExpenseMediaAccessService {
	String issueGetPath(String mediaKey, Duration expiration);
}
