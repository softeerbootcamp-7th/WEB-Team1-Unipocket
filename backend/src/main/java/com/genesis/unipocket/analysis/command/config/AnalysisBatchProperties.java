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

	public enum OutlierMethod {
		IQR,
		MAD
	}

	private boolean enabled = true;
	private int lookbackMonths = 3;
	private int runHour = 0;
	private int runMinute = 10;
	private int dispatchBatchSize = 20;
	private int leaseMinutes = 30;
	private int maxRetry = 7;
	private int retryBaseMinutes = 1;
	private BigDecimal cleanedMinAmount = new BigDecimal("0.01");
	private BigDecimal cleanedMaxAmount = new BigDecimal("100000000");
	private OutlierMethod outlierMethod = OutlierMethod.IQR;
	private int outlierMinSampleSize = 30;
	private double outlierLowerTailP = 0.01d;
	private double outlierUpperTailP = 0.01d;
	private double outlierIqrMultiplier = 1.5d;
	private double outlierMadZThreshold = 3.5d;
	private int outlierReferenceDays = 90;
}
