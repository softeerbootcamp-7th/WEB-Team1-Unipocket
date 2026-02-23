package com.genesis.unipocket.analysis.command.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "analysis.batch")
public class AnalysisBatchProperties {

	private boolean enabled;
	private int runHour;
	private int runMinute;
	private int dispatchBatchSize;
	private int leaseMinutes;
	private int maxRetry;
	private int retryBaseMinutes;
	private double outlierIqrMultiplier;
	private int peerMinSampleSize;
	private int maxLoopsPerBatch;
}
