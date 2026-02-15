package com.genesis.unipocket.global.common.converter;

import com.genesis.unipocket.global.common.enums.Category;
import org.springframework.core.convert.converter.Converter;

public class CategoryConverter implements Converter<String, Category> {
	@Override
	public Category convert(String source) {
		try {
			int ordinal = Integer.parseInt(source);
			return Category.values()[ordinal];
		} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
			// 변환 실패 시 null 혹은 예외 처리
			return null;
		}
	}
}
