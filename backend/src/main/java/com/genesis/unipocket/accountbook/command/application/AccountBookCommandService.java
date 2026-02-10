package com.genesis.unipocket.accountbook.command.application;

import com.genesis.unipocket.accountbook.command.application.command.CreateAccountBookCommand;
import com.genesis.unipocket.accountbook.command.application.command.DeleteAccountBookCommand;
import com.genesis.unipocket.accountbook.command.application.command.UpdateAccountBookCommand;
import com.genesis.unipocket.accountbook.command.application.validator.AccountBookValidator;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookCreateArgs;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.command.persistence.repository.AccountBookCommandRepository;
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
public class AccountBookCommandService {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+)$");
    private static final String DEFAULT_NAME_SUFFIX = "의 가계부";
    private static final CountryCode DEFAULT_BASE_COUNTRY_CODE = CountryCode.KR;

    private final AccountBookCommandRepository repository;
    private final AccountBookValidator validator;

    @Transactional
    public Long create(CreateAccountBookCommand command) {

        String uniqueTitle = getUniqueTitle(
                command.userId().toString(), command.userName() + DEFAULT_NAME_SUFFIX);

        AccountBookCreateArgs args = new AccountBookCreateArgs(
                command.userId().toString(),
                uniqueTitle,
                command.localCountryCode(),
                DEFAULT_BASE_COUNTRY_CODE,
                command.startDate(),
                command.endDate());

        AccountBookEntity newEntity = AccountBookEntity.create(args);
        validator.validate(newEntity);
        AccountBookEntity savedEntity = repository.save(newEntity);

        return savedEntity.getId();
    }

    @Transactional
    public Long update(UpdateAccountBookCommand command) {

        AccountBookEntity entity = findAndVerifyOwnership(command.accountBookId(), command.userId().toString());

        entity.updateBudget(command.budget());
        entity.updateTitle(command.title());
        entity.changeAccountBookPeriod(command.startDate(), command.endDate());
        entity.updateCountryCodes(command.localCountryCode(), command.baseCountryCode());

        validator.validate(entity);

        return entity.getId();
    }

    @Transactional
    public void delete(DeleteAccountBookCommand command) {
        AccountBookEntity entity = findAndVerifyOwnership(command.accountBookId(), command.userId().toString());

        repository.delete(entity);
    }

    private AccountBookEntity findAndVerifyOwnership(Long accountBookId, String userId) {
        AccountBookEntity entity = repository
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
            if (name.equals(baseTitle))
                continue;

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
