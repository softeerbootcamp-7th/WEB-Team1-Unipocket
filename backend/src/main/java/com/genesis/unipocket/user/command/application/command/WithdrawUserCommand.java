package com.genesis.unipocket.user.command.application.command;

import java.util.UUID;

public record WithdrawUserCommand(UUID userId) {
    public static WithdrawUserCommand from(UUID userId) {
        return new WithdrawUserCommand(userId);
    }
}
