package com.genesis.unipocket.accountbook.service;

import com.genesis.unipocket.accountbook.facade.dto.AccountBookDto;
import com.genesis.unipocket.accountbook.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.persistence.repository.AccountBookJpaRepository;
import com.genesis.unipocket.accountbook.presentation.dto.request.CreateAccountBookReq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>가계부 서비스</b>
 * <p>
 * 가계부 서비스의 애플리케이션적 도메인(DB로 제어하기 어려운 비즈니스적 생성 범위, 값의 범위 등) 로직에 관여
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AccountBookService {
	private final AccountBookJpaRepository repository;

	@Transactional
	public AccountBookDto create(long userId, CreateAccountBookReq req) {

		String title = getUniqueTitle(req.getTitle());

		AccountBookEntity newEntity =
				AccountBookEntity.create(
						userId,
						title,
						req.getLocalCountryCode(),
						req.getBaseCountryCode(),
						req.getStartDate(),
						req.getEndDate());

		AccountBookEntity savedEntity = repository.save(newEntity);

		return AccountBookDto.from(savedEntity);
	}

	private String getUniqueTitle(String title) {
		String baseName = title.replaceAll("\\s\\(\\d+\\)$", "");

		List<String> existingNames = repository.findNamesStartingWith(baseName);

		// 중복이 없으면 바로 반환
		if (!existingNames.contains(title)) {
			return title;
		}

		// 최대 숫자 탐색
		int maxNum = 0;
		Pattern pattern = Pattern.compile(Pattern.quote(baseName) + "\\s\\((\\d+)\\)$");

		for (String name : existingNames) {
			Matcher matcher = pattern.matcher(name);
			if (matcher.matches()) {
				maxNum = Math.max(maxNum, Integer.parseInt(matcher.group(1)));
			}
		}

		return String.format("%s (%d)", baseName, maxNum + 1);
	}
}
