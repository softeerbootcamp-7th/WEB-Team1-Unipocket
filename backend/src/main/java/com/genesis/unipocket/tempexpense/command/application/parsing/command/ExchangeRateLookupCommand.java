package com.genesis.unipocket.tempexpense.command.application.parsing.command;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.time.LocalDate;

public record ExchangeRateLookupCommand(
		CurrencyCode fromCurrencyCode, CurrencyCode toCurrencyCode, LocalDate date) {}
