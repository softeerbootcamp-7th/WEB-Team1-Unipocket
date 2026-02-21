package com.genesis.unipocket.accountbook.command.facade.provide;

import com.genesis.unipocket.accountbook.command.persistence.repository.AccountBookCommandRepository;
import com.genesis.unipocket.analysis.command.facade.port.AnalysisAccountBookReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnalysisAccountBookProvider implements AnalysisAccountBookReadService {

	private final AccountBookCommandRepository accountBookCommandRepository;

	@Override
	public AccountBookCountryInfo getRequiredCountryInfo(Long accountBookId) {
		var accountBook =
				accountBookCommandRepository
						.findById(accountBookId)
						.orElseThrow(
								() ->
										new IllegalStateException(
												"Account book not found while processing analysis: "
														+ accountBookId));
		return new AccountBookCountryInfo(
				accountBook.getId(), accountBook.getLocalCountryCode(), accountBook.getBaseCountryCode());
	}
}
