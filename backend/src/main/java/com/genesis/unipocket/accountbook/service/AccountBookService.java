package com.genesis.unipocket.accountbook.service;

import com.genesis.unipocket.accountbook.dto.common.AccountBookDto;
import com.genesis.unipocket.accountbook.dto.request.CreateAccountBookReq;
import com.genesis.unipocket.accountbook.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.repository.AccountBookRepository;
import com.genesis.unipocket.accountbook.validator.AccountBookValidator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountBookService {

	private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+)$");
	private static final String BASE_NAME_SUFFIX = "의 가계부";
	private final AccountBookRepository repository;
	private final AccountBookValidator validator;

	@Transactional
	public AccountBookDto create(long userId, String username, CreateAccountBookReq req) {
		String uniqueTitle = getUniqueTitle(userId, username + BASE_NAME_SUFFIX);

		AccountBookEntity newEntity =
				AccountBookEntity.create(
						userId,
						uniqueTitle,
						req.localCountryCode(),
						req.baseCountryCode(),
						req.startDate(),
						req.endDate());

		validator.validate(newEntity);

		AccountBookEntity savedEntity = repository.save(newEntity);

		return AccountBookDto.from(savedEntity);
	}

	private String getUniqueTitle(long userId, String baseTitle) {
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
