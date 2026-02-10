package com.genesis.unipocket.user.command.application.command;

import java.util.UUID;

public record DeleteCardCommand(Long cardId, UUID userId) {
	public static DeleteCardCommand of(Long cardId, UUID userId) {
		return new DeleteCardCommand(cardId, userId);
	}
}
