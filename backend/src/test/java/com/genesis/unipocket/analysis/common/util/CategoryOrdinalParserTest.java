package com.genesis.unipocket.analysis.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.genesis.unipocket.global.common.enums.Category;
import org.junit.jupiter.api.Test;

class CategoryOrdinalParserTest {

	@Test
	void parse_nullValue_returnsNull() {
		assertThat(CategoryOrdinalParser.parse(null)).isNull();
	}

	@Test
	void parse_numericOrdinalWithinRange_returnsOrdinal() {
		assertThat(CategoryOrdinalParser.parse(3)).isEqualTo(3);
	}

	@Test
	void parse_categoryName_returnsOrdinal() {
		assertThat(CategoryOrdinalParser.parse("FOOD")).isEqualTo(Category.FOOD.ordinal());
	}

	@Test
	void parse_invalidCategoryName_returnsNull() {
		assertThat(CategoryOrdinalParser.parse("UNKNOWN_CATEGORY")).isNull();
	}

	@Test
	void parse_outOfRangeOrdinal_returnsNull() {
		assertThat(CategoryOrdinalParser.parse(Category.values().length)).isNull();
		assertThat(CategoryOrdinalParser.parse(-1)).isNull();
	}
}
