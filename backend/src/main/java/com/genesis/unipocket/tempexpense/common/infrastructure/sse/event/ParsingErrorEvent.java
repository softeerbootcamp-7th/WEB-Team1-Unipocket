package com.genesis.unipocket.tempexpense.common.infrastructure.sse.event;

public record ParsingErrorEvent(String message, String code, Integer status) {

	public ParsingErrorEvent(String message) {
		this(message, null, null);
	}
}
