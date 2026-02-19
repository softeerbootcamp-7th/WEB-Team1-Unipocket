package com.genesis.unipocket.exchange.command.application.impl.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YahooChartResponse(Chart chart) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Chart(List<Result> result, ChartError error) {}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Result(List<Long> timestamp, Indicators indicators) {}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Indicators(List<Quote> quote) {}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Quote(List<BigDecimal> close) {}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record ChartError(String code, String description) {}
}
