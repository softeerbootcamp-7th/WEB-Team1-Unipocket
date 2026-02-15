package com.genesis.unipocket.analysis.command.application;

import com.genesis.unipocket.analysis.command.config.AnalysisBatchProperties;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisMetricType;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisQualityType;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisResultCacheService {

	private static final String COUNTRY_KEY_PREFIX = "analysis:country";
	private static final String ACCOUNT_KEY_PREFIX = "analysis:account";

	private final RedisTemplate<String, String> redisTemplate;
	private final AnalysisBatchProperties properties;

	public void cacheCountryMetric(
			CountryCode countryCode,
			LocalDate targetDate,
			AnalysisMetricType metricType,
			AnalysisQualityType qualityType,
			BigDecimal metricValue) {
		if (!properties.isCacheEnabled()) {
			return;
		}

		String key =
				COUNTRY_KEY_PREFIX
						+ ":"
						+ countryCode.name()
						+ ":"
						+ targetDate
						+ ":"
						+ qualityType.name()
						+ ":"
						+ metricType.name();
		cacheValue(key, metricValue);
	}

	public void cacheAccountMetric(
			Long accountBookId,
			LocalDate targetDate,
			AnalysisMetricType metricType,
			AnalysisQualityType qualityType,
			BigDecimal metricValue) {
		if (!properties.isCacheEnabled()) {
			return;
		}

		String key =
				ACCOUNT_KEY_PREFIX
						+ ":"
						+ accountBookId
						+ ":"
						+ targetDate
						+ ":"
						+ qualityType.name()
						+ ":"
						+ metricType.name();
		cacheValue(key, metricValue);
	}

	public Optional<BigDecimal> getCountryMetric(
			CountryCode countryCode,
			LocalDate targetDate,
			AnalysisMetricType metricType,
			AnalysisQualityType qualityType) {
		String key =
				COUNTRY_KEY_PREFIX
						+ ":"
						+ countryCode.name()
						+ ":"
						+ targetDate
						+ ":"
						+ qualityType.name()
						+ ":"
						+ metricType.name();
		return getValue(key);
	}

	public Optional<BigDecimal> getAccountMetric(
			Long accountBookId,
			LocalDate targetDate,
			AnalysisMetricType metricType,
			AnalysisQualityType qualityType) {
		String key =
				ACCOUNT_KEY_PREFIX
						+ ":"
						+ accountBookId
						+ ":"
						+ targetDate
						+ ":"
						+ qualityType.name()
						+ ":"
						+ metricType.name();
		return getValue(key);
	}

	private void cacheValue(String key, BigDecimal value) {
		try {
			Duration ttl = Duration.ofDays(Math.max(1, properties.getCacheTtlDays()));
			redisTemplate.opsForValue().set(key, value.toPlainString(), ttl);
		} catch (Exception e) {
			log.warn("Failed to cache analysis metric. key={}", key, e);
		}
	}

	private Optional<BigDecimal> getValue(String key) {
		try {
			String value = redisTemplate.opsForValue().get(key);
			if (value == null) {
				return Optional.empty();
			}
			return Optional.of(new BigDecimal(value));
		} catch (Exception e) {
			log.warn("Failed to read analysis metric cache. key={}", key, e);
			return Optional.empty();
		}
	}
}
