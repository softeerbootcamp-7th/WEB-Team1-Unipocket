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

	private boolean enabled = true;
	private int runHour = 3;
	private int runMinute = 0;
	private int dispatchBatchSize = 20;
	private int leaseMinutes = 30;
	private int maxRetry = 7;
	private int retryBaseMinutes = 1;
	private double outlierIqrMultiplier = 1.5d;
	private int peerMinSampleSize = 10;
}
