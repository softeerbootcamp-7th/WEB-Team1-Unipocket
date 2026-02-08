package com.genesis.unipocket.accountbook.service;

import com.genesis.unipocket.accountbook.dto.common.AccountBookDto;
import com.genesis.unipocket.accountbook.dto.converter.AccountBookDtoConverter;
import com.genesis.unipocket.accountbook.dto.request.AccountBookCreateRequest;
import com.genesis.unipocket.accountbook.dto.request.AccountBookUpdateRequest;
import com.genesis.unipocket.accountbook.persistence.entity.AccountBookCreateArgs;
import com.genesis.unipocket.accountbook.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.persistence.repository.AccountBookRepository;
import com.genesis.unipocket.accountbook.service.validator.AccountBookValidator;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
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
	private static final String DEFAULT_NAME_SUFFIX = "의 가계부";
	private static final CountryCode DEFAULT_BASE_COUNTRY_CODE = CountryCode.KR;

	private final AccountBookRepository repository;
	private final AccountBookValidator validator;
	private final AccountBookDtoConverter converter;

	@Transactional
	public AccountBookDto create(String userId, String username, AccountBookCreateRequest req) {

		String uniqueTitle = getUniqueTitle(userId, username + DEFAULT_NAME_SUFFIX);

		AccountBookCreateArgs args =
				new AccountBookCreateArgs(
						userId,
						uniqueTitle,
						req.localCountryCode(),
						DEFAULT_BASE_COUNTRY_CODE,
						req.startDate(),
						req.endDate());

		AccountBookEntity newEntity = AccountBookEntity.create(args);
		validator.validate(newEntity);
		AccountBookEntity savedEntity = repository.save(newEntity);

		return converter.toDto(savedEntity);
	}

	@Transactional
	public AccountBookDto update(Long accountBookId, String userId, AccountBookUpdateRequest req) {

		AccountBookEntity entity = findAndVerifyOwnership(accountBookId, userId);

		entity.updateBudget(req.budget());
		entity.updateTitle(req.title());
		entity.changeAccountBookPeriod(req.startDate(), req.endDate());
		entity.updateCountryCodes(req.localCountryCode(), req.baseCountryCode());

		validator.validate(entity);

		return converter.toDto(entity);
	}

	@Transactional
	public void delete(Long accountBookId, String userId) {
		AccountBookEntity entity = findAndVerifyOwnership(accountBookId, userId);

		repository.delete(entity);
	}

	@Transactional(readOnly = true)
	public AccountBookDto getAccountBook(Long accountBookId, String userId) {
		AccountBookEntity entity = findAndVerifyOwnership(accountBookId, userId);
		return converter.toDto(entity);
	}

	@Transactional(readOnly = true)
	public List<AccountBookDto> getAccountBooks(String userId) {
		return repository.findAllByUserId(userId).stream().map(converter::toDto).toList();
	}

	private AccountBookEntity findAndVerifyOwnership(Long accountBookId, String userId) {
		AccountBookEntity entity =
				repository
						.findById(accountBookId)
						.orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_BOOK_NOT_FOUND));
		if (!entity.getUserId().equals(userId)) {
			throw new BusinessException(ErrorCode.ACCOUNT_BOOK_UNAUTHORIZED_ACCESS);
		}
		return entity;
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
