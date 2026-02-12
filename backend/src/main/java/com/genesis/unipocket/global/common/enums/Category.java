package com.genesis.unipocket.global.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Category {
	UNCLASSIFIED("미분류"),
	RESIDENCE("거주"),
	FOOD("식비"),
	TRANSPORT("교통비"),
	LIVING("생활"),
	LEISURE("여가"),
	SHOPPING("쇼핑"),
	COMMUNICATION("통신비"),
	ACADEMIC("학교"),
	INCOME("수입");

	private final String name;

	Category(String name) {
		this.name = name;
	}

	@JsonValue
	public int getOrdinal() {
		return this.ordinal();
	}
}
