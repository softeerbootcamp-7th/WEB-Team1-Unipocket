package com.genesis.unipocket.tempexpense.common.infrastructure.sse.event;

public record ParsingErrorEvent(
		String message, String code, Integer status, String lastCode, String lastFileKey) {

	public ParsingErrorEvent(String message) {
		this(message, null, null, null, null);
	}

	public ParsingErrorEvent(String message, String code, Integer status) {
		this(message, code, status, null, null);
	}
}
