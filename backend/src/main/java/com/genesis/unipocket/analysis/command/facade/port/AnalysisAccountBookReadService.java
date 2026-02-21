package com.genesis.unipocket.analysis.command.facade.port;

import com.genesis.unipocket.global.common.enums.CountryCode;

public interface AnalysisAccountBookReadService {

	AccountBookCountryInfo getRequiredCountryInfo(Long accountBookId);

	record AccountBookCountryInfo(
			Long accountBookId, CountryCode localCountryCode, CountryCode baseCountryCode) {}
}
