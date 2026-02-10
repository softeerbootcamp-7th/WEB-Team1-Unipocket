package com.genesis.unipocket.accountbook.command.application.command;

import com.genesis.unipocket.accountbook.command.presentation.request.AccountBookCreateRequest;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDate;
import java.util.UUID;

public record CreateAccountBookCommand(
        UUID userId,
        String userName,
        CountryCode localCountryCode,
        LocalDate startDate,
        LocalDate endDate) {

    public static CreateAccountBookCommand of(
            UUID userId, String userName, AccountBookCreateRequest request) {
        return new CreateAccountBookCommand(
                userId,
                userName,
                request.localCountryCode(),
                request.startDate(),
                request.endDate());
    }
}
