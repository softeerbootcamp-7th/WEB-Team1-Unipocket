package com.genesis.unipocket.accountbook.command.application;

import com.genesis.unipocket.accountbook.command.application.converter.AccountBookApplicationConverter;
import com.genesis.unipocket.accountbook.command.application.dto.AccountBookCreateCommand;
import com.genesis.unipocket.accountbook.command.application.dto.AccountBookCreateResult;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.command.persistence.repository.AccountBookRepository;
import com.genesis.unipocket.accountbook.command.persistence.validator.AccountBookValidator;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountBookCommandService {

	private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+)$");
	private static final String DEFAULT_NAME_SUFFIX = "의 가계부";
	private static final CountryCode DEFAULT_BASE_COUNTRY_CODE = CountryCode.KR;

	private final AccountBookRepository repository;
	private final AccountBookValidator validator;
	private final AccountBookApplicationConverter converter;

	@Transactional
	public AccountBookCreateResult create(AccountBookCreateCommand params) {
		String uniqueTitle =
				getUniqueTitle(params.userId(), params.username() + DEFAULT_NAME_SUFFIX);

		AccountBookEntity newEntity =
				AccountBookEntity.create(
						params.userId(),
						uniqueTitle,
						params.localCountryCode(),
						DEFAULT_BASE_COUNTRY_CODE,
						params.startDate(),
						params.endDate());

		validator.validate(newEntity);

		AccountBookEntity savedEntity = repository.save(newEntity);

		return converter.toResult(savedEntity);
	}

	private String getUniqueTitle(String userId, String baseTitle) {
		List<String> existingNames = repository.findNamesStartingWith(userId, baseTitle);

		if (existingNames.isEmpty()) {
			return baseTitle + "1";
		}

		int maxNum = 0;

		for (String name : existingNames) {
			if (name.equals(baseTitle)) continue;

			Matcher matcher = NUMBER_PATTERN.matcher(name);
			if (matcher.find()) {
				try {
					int num = Integer.parseInt(matcher.group(1));
					maxNum = Math.max(maxNum, num);
				} catch (NumberFormatException e) {
					// 숫자가 너무 커서 파싱 불가능한 경우는 무시
				}
			}
		}

		return baseTitle + (maxNum + 1);
	}
}
