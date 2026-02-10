package com.genesis.unipocket.accountbook.command.application.command;

import java.util.UUID;

public record DeleteAccountBookCommand(Long accountBookId, UUID userId) {
    public static DeleteAccountBookCommand of(Long accountBookId, UUID userId) {
        return new DeleteAccountBookCommand(accountBookId, userId);
    }
}
