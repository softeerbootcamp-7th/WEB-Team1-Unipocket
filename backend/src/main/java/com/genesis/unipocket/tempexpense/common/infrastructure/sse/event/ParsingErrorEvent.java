package com.genesis.unipocket.tempexpense.common.infrastructure.sse.event;

public record ParsingErrorEvent(
		String message, String code, Integer status, String lastCode, String lastFileKey) {}
