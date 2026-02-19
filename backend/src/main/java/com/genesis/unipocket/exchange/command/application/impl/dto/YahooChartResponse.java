package com.genesis.unipocket.exchange.command.application.impl.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;

// Yahoo 응답에서 모르는 필드는 파싱 시 무시한다.
@JsonIgnoreProperties(ignoreUnknown = true)
// 최상위 chart 노드만 매핑하는 DTO다.
public record YahooChartResponse(Chart chart) {

	// chart 하위에서도 모르는 필드를 무시한다.
	@JsonIgnoreProperties(ignoreUnknown = true)
	// result 목록과 error 노드를 함께 담는다.
	public record Chart(List<Result> result, ChartError error) {}

	// result 노드에서도 모르는 필드를 무시한다.
	@JsonIgnoreProperties(ignoreUnknown = true)
	// timestamp 배열과 indicators 노드를 매핑한다.
	public record Result(List<Long> timestamp, Indicators indicators) {}

	// indicators 노드에서도 모르는 필드를 무시한다.
	@JsonIgnoreProperties(ignoreUnknown = true)
	// quote 배열을 매핑한다.
	public record Indicators(List<Quote> quote) {}

	// quote 노드에서도 모르는 필드를 무시한다.
	@JsonIgnoreProperties(ignoreUnknown = true)
	// 종가(close) 배열만 사용하므로 해당 필드만 매핑한다.
	public record Quote(List<BigDecimal> close) {}

	// error 노드에서도 모르는 필드를 무시한다.
	@JsonIgnoreProperties(ignoreUnknown = true)
	// 오류 코드/설명을 매핑한다.
	public record ChartError(String code, String description) {}
}
