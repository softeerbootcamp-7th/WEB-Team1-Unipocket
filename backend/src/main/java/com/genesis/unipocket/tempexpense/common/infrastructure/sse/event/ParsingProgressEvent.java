package com.genesis.unipocket.tempexpense.common.infrastructure.sse.event;

public record ParsingProgressEvent(int progress, String message, String code, String fileKey) {

	public ParsingProgressEvent(int progress) {
		this(progress, null, null, null);
	}

	public ParsingProgressEvent(int progress, String message) {
		this(progress, message, null, null);
	}

	public ParsingProgressEvent(int progress, String message, String code) {
		this(progress, message, code, null);
	}
}
