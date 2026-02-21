package com.genesis.unipocket.expense.query.facade.port;

import java.time.Duration;

public interface ExpenseMediaAccessService {

	String issueGetPath(String mediaKey, Duration expiration);
}
