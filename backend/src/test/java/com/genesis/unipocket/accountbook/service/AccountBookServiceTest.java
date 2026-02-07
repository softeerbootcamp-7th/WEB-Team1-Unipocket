package com.genesis.unipocket.accountbook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.genesis.unipocket.accountbook.dto.common.AccountBookDto;
import com.genesis.unipocket.accountbook.dto.converter.AccountBookDtoConverter;
import com.genesis.unipocket.accountbook.dto.request.AccountBookCreateRequest;
import com.genesis.unipocket.accountbook.dto.request.AccountBookUpdateRequest;
import com.genesis.unipocket.accountbook.entity.AccountBookCreateArgs;
import com.genesis.unipocket.accountbook.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.repository.AccountBookRepository;
import com.genesis.unipocket.accountbook.service.validator.AccountBookValidator;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountBookService 단위 테스트")
public class AccountBookServiceTest {

	@Mock private AccountBookRepository repository;
	@Mock private AccountBookValidator validator;
	@Mock private AccountBookDtoConverter converter;

	@InjectMocks private AccountBookService accountBookService;

	private final String userId = UUID.randomUUID().toString();
	private final String username = "testUser";

	@Test
	@DisplayName("가계부 생성 - 성공")
	void create_Success() {
		// given
		AccountBookCreateRequest req =
				new AccountBookCreateRequest(
						CountryCode.US, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31));

		String expectedTitle = username + "의 가계부1";
		AccountBookCreateArgs args =
				new AccountBookCreateArgs(
						userId,
						expectedTitle,
						req.localCountryCode(),
						CountryCode.KR,
						req.startDate(),
						req.endDate());
		AccountBookEntity savedEntity = AccountBookEntity.create(args);
		AccountBookDto expectedDto =
				new AccountBookDto(
						1L,
						expectedTitle,
						req.localCountryCode(),
						CountryCode.KR,
						0L,
						req.startDate(),
						req.endDate());

		given(repository.findNamesStartingWith(any(), any())).willReturn(Collections.emptyList());
		given(repository.save(any(AccountBookEntity.class))).willReturn(savedEntity);
		given(converter.toDto(any(AccountBookEntity.class))).willReturn(expectedDto);

		// when
		AccountBookDto result = accountBookService.create(userId, username, req);

		// then
		assertThat(result).isEqualTo(expectedDto);
		verify(validator).validate(any(AccountBookEntity.class));
		verify(repository).save(any(AccountBookEntity.class));
	}

	@Test
	@DisplayName("가계부 생성 - 이름 중복 시 번호 증가")
	void create_DuplicateName() {
		// given
		AccountBookCreateRequest req =
				new AccountBookCreateRequest(
						CountryCode.US, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31));

		String baseTitle = username + "의 가계부";
		given(repository.findNamesStartingWith(userId, baseTitle))
				.willReturn(List.of(baseTitle + "1", baseTitle + "2"));

		String expectedTitle = baseTitle + "3";
		AccountBookDto expectedDto =
				new AccountBookDto(
						1L,
						expectedTitle,
						req.localCountryCode(),
						CountryCode.KR,
						0L,
						req.startDate(),
						req.endDate());

		given(repository.save(any(AccountBookEntity.class)))
				.willReturn(
						AccountBookEntity.create(
								new AccountBookCreateArgs(
										userId,
										expectedTitle,
										CountryCode.US,
										CountryCode.KR,
										LocalDate.now(),
										LocalDate.now()))); // Mocking return is flexible here since
		// we verify logic
		given(converter.toDto(any(AccountBookEntity.class))).willReturn(expectedDto);

		// when
		AccountBookDto result = accountBookService.create(userId, username, req);

		// then
		assertThat(result.title()).isEqualTo(expectedTitle);
	}

	@Test
	@DisplayName("가계부 수정 - 성공")
	void update_Success() {
		// given
		Long accountBookId = 1L;
		AccountBookUpdateRequest req =
				new AccountBookUpdateRequest(
						"New Title",
						CountryCode.JP,
						CountryCode.KR,
						1000L,
						LocalDate.of(2023, 2, 1),
						LocalDate.of(2023, 11, 30));

		AccountBookEntity entity =
				AccountBookEntity.create(
						new AccountBookCreateArgs(
								userId,
								"Old Title",
								CountryCode.US,
								CountryCode.KR,
								LocalDate.of(2023, 1, 1),
								LocalDate.of(2023, 12, 31)));

		// Reflection or setter needed if ID is not set in create. Assuming Mocking or simple obj.
		// Since Entity create usually doesn't set ID. But repository mock returns it.
		// Here we just use the entity object.

		given(repository.findById(accountBookId)).willReturn(Optional.of(entity));

		AccountBookDto expectedDto =
				new AccountBookDto(
						accountBookId,
						req.title(),
						req.localCountryCode(),
						req.baseCountryCode(),
						req.budget(),
						req.startDate(),
						req.endDate());
		given(converter.toDto(entity)).willReturn(expectedDto);

		// when
		AccountBookDto result = accountBookService.update(accountBookId, userId, req);

		// then
		assertThat(result.title()).isEqualTo(req.title());
		assertThat(result.localCountryCode()).isEqualTo(req.localCountryCode());
		verify(validator).validate(entity);
	}

	@Test
	@DisplayName("가계부 수정 - 존재하지 않는 가계부")
	void update_NotFound() {
		// given
		Long accountBookId = 1L;
		AccountBookUpdateRequest req =
				new AccountBookUpdateRequest(
						"Title",
						CountryCode.US,
						CountryCode.KR,
						1000L,
						LocalDate.now(),
						LocalDate.now());

		given(repository.findById(accountBookId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> accountBookService.update(accountBookId, userId, req))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_BOOK_NOT_FOUND);
	}

	@Test
	@DisplayName("가계부 수정 - 권한 없음")
	void update_Unauthorized() {
		// given
		Long accountBookId = 1L;
		String otherUserId = "otherUser";
		AccountBookUpdateRequest req =
				new AccountBookUpdateRequest(
						"Title",
						CountryCode.US,
						CountryCode.KR,
						1000L,
						LocalDate.now(),
						LocalDate.now());

		AccountBookEntity entity =
				AccountBookEntity.create(
						new AccountBookCreateArgs(
								otherUserId,
								"Title",
								CountryCode.US,
								CountryCode.KR,
								LocalDate.now(),
								LocalDate.now()));

		given(repository.findById(accountBookId)).willReturn(Optional.of(entity));

		// when & then
		assertThatThrownBy(() -> accountBookService.update(accountBookId, userId, req))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_BOOK_UNAUTHORIZED_ACCESS);
	}

	@Test
	@DisplayName("가계부 삭제 - 성공")
	void delete_Success() {
		// given
		Long accountBookId = 1L;
		AccountBookEntity entity =
				AccountBookEntity.create(
						new AccountBookCreateArgs(
								userId,
								"Title",
								CountryCode.US,
								CountryCode.KR,
								LocalDate.now(),
								LocalDate.now()));

		given(repository.findById(accountBookId)).willReturn(Optional.of(entity));

		// when
		accountBookService.delete(accountBookId, userId);

		// then
		verify(repository).delete(entity);
	}

	@Test
	@DisplayName("가계부 조회 - 성공")
	void getAccountBook_Success() {
		// given
		Long accountBookId = 1L;
		AccountBookEntity entity =
				AccountBookEntity.create(
						new AccountBookCreateArgs(
								userId,
								"Title",
								CountryCode.US,
								CountryCode.KR,
								LocalDate.now(),
								LocalDate.now()));

		AccountBookDto expectedDto =
				new AccountBookDto(
						accountBookId,
						"Title",
						CountryCode.US,
						CountryCode.KR,
						0L,
						LocalDate.now(),
						LocalDate.now());

		given(repository.findById(accountBookId)).willReturn(Optional.of(entity));
		given(converter.toDto(entity)).willReturn(expectedDto);

		// when
		AccountBookDto result = accountBookService.getAccountBook(accountBookId, userId);

		// then
		assertThat(result).isEqualTo(expectedDto);
	}

	@Test
	@DisplayName("내 가계부 목록 조회 - 성공")
	void getAccountBooks_Success() {
		// given
		AccountBookEntity entity1 =
				AccountBookEntity.create(
						new AccountBookCreateArgs(
								userId,
								"Title1",
								CountryCode.US,
								CountryCode.KR,
								LocalDate.now(),
								LocalDate.now()));
		AccountBookEntity entity2 =
				AccountBookEntity.create(
						new AccountBookCreateArgs(
								userId,
								"Title2",
								CountryCode.JP,
								CountryCode.KR,
								LocalDate.now(),
								LocalDate.now()));

		given(repository.findAllByUserId(userId)).willReturn(List.of(entity1, entity2));
		given(converter.toDto(any(AccountBookEntity.class)))
				.willReturn(
						new AccountBookDto(
								1L,
								"Title1",
								CountryCode.US,
								CountryCode.KR,
								0L,
								LocalDate.now(),
								LocalDate.now()));

		// when
		List<AccountBookDto> result = accountBookService.getAccountBooks(userId);

		// then
		assertThat(result).hasSize(2);
	}
}
