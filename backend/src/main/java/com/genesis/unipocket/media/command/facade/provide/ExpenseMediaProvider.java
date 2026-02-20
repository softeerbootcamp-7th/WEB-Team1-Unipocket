package com.genesis.unipocket.media.command.facade.provide;

import com.genesis.unipocket.expense.query.port.ExpenseMediaAccessService;
import com.genesis.unipocket.media.command.application.MediaObjectStorage;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExpenseMediaProvider implements ExpenseMediaAccessService {

	private final MediaObjectStorage mediaObjectStorage;

	@Override
	public String issueGetPath(String mediaKey, Duration expiration) {
		return mediaObjectStorage.getPresignedGetUrl(mediaKey, expiration);
	}
}
