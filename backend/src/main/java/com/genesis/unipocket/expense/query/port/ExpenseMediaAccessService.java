package com.genesis.unipocket.expense.query.port;

import java.time.Duration;

public interface ExpenseMediaAccessService {

	String issueGetPath(String mediaKey, Duration expiration);
}
