package com.genesis.unipocket.expense.application;

import static org.assertj.core.api.Assertions.*;

import com.genesis.unipocket.expense.service.CsvParsingService;
import com.genesis.unipocket.global.infrastructure.gemini.GeminiService.ParsedExpenseItem;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * <b>CsvParsingService 단위 테스트</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
class CsvParsingServiceTest {

	private final CsvParsingService service = new CsvParsingService();

	@Test
	@DisplayName("기본 CSV 파싱 성공")
	void parseCsv_Basic_Success() {
		// given
		String csv =
				"""
				날짜,가맹점,금액,통화,카테고리,메모
				2026-02-08,스타벅스,5000,KRW,FOOD,회의비
				2026-02-07,편의점,3000,KRW,FOOD,간식
				""";

		// when
		List<ParsedExpenseItem> result = service.parseCsv(csv);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).merchantName()).isEqualTo("스타벅스");
		assertThat(result.get(0).localAmount()).isEqualTo(BigDecimal.valueOf(5000));
		assertThat(result.get(1).merchantName()).isEqualTo("편의점");
	}

	@Test
	@DisplayName("영문 헤더 CSV 파싱 성공")
	void parseCsv_EnglishHeaders_Success() {
		// given
		String csv =
				"""
				date,merchant,amount,currency,category,memo
				2026-02-08,Starbucks,5000,KRW,FOOD,Meeting
				""";

		// when
		List<ParsedExpenseItem> result = service.parseCsv(csv);

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).merchantName()).isEqualTo("Starbucks");
	}

	@Test
	@DisplayName("따옴표가 포함된 CSV 파싱 성공")
	void parseCsv_WithQuotes_Success() {
		// given
		String csv = """
				날짜,가맹점,금액,메모
				2026-02-08,"스타벅스 강남점",5000,"회의비, 커피"
				""";

		// when
		List<ParsedExpenseItem> result = service.parseCsv(csv);

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).merchantName()).isEqualTo("스타벅스 강남점");
		assertThat(result.get(0).memo()).isEqualTo("회의비, 커피");
	}

	@Test
	@DisplayName("빈 CSV 처리")
	void parseCsv_Empty_ReturnsEmptyList() {
		// given
		String csv = "";

		// when
		List<ParsedExpenseItem> result = service.parseCsv(csv);

		// then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("헤더만 있는 CSV 처리")
	void parseCsv_HeaderOnly_ReturnsEmptyList() {
		// given
		String csv = "날짜,가맹점,금액";

		// when
		List<ParsedExpenseItem> result = service.parseCsv(csv);

		// then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("금액 파싱 - 통화 기호 제거")
	void parseCsv_AmountWithSymbol_Success() {
		// given
		String csv = """
				가맹점,금액
				스타벅스,"$5,000"
				편의점,₩3000
				""";

		// when
		List<ParsedExpenseItem> result = service.parseCsv(csv);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).localAmount()).isEqualTo(BigDecimal.valueOf(5000));
		assertThat(result.get(1).localAmount()).isEqualTo(BigDecimal.valueOf(3000));
	}

	@Test
	@DisplayName("다양한 날짜 포맷 파싱")
	void parseCsv_VariousDateFormats_Success() {
		// given
		String csv =
				"""
				날짜,가맹점,금액
				2026-02-08 10:30:00,스타벅스,5000
				2026/02/07,편의점,3000
				2026-02-06 14:00,카페,4000
				""";

		// when
		List<ParsedExpenseItem> result = service.parseCsv(csv);

		// then
		assertThat(result).hasSize(3);
		assertThat(result.get(0).occurredAt()).isNotNull();
		assertThat(result.get(1).occurredAt()).isNotNull();
		assertThat(result.get(2).occurredAt()).isNotNull();
	}

	@Test
	@DisplayName("필수 필드 누락 시 해당 행 스킵")
	void parseCsv_MissingRequired_SkipsRow() {
		// given
		String csv = """
				가맹점,금액
				스타벅스,5000
				,3000
				편의점,
				카페,2000
				""";

		// when
		List<ParsedExpenseItem> result = service.parseCsv(csv);

		// then
		// 가맹점 또는 금액이 없는 행들은 스킵됨
		assertThat(result).hasSizeLessThanOrEqualTo(2);
	}
}
