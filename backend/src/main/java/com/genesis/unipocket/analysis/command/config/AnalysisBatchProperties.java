package com.genesis.unipocket.analysis.command.config;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "analysis.batch")
public class AnalysisBatchProperties {

	private boolean enabled = true;
	private int lookbackMonths = 3;
	private int runHour = 0;
	private int runMinute = 10;
	private int dispatchBatchSize = 20;
	private int maxRetry = 7;
	private int retryBaseMinutes = 1;
	private BigDecimal cleanedMinAmount = new BigDecimal("0.01");
	private BigDecimal cleanedMaxAmount = new BigDecimal("100000000");
	private boolean cacheEnabled = true;
	private int cacheTtlDays = 120;
}
