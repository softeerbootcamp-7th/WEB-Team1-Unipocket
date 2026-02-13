package com.genesis.unipocket.accountbook.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.genesis.unipocket.accountbook.command.application.command.CreateAccountBookCommand;
import com.genesis.unipocket.accountbook.command.application.command.DeleteAccountBookCommand;
import com.genesis.unipocket.accountbook.command.application.command.UpdateAccountBookCommand;
import com.genesis.unipocket.accountbook.command.application.validator.AccountBookValidator;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookCreateArgs;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.command.persistence.repository.AccountBookCommandRepository;
import com.genesis.unipocket.accountbook.command.presentation.request.AccountBookCreateRequest;
import com.genesis.unipocket.accountbook.command.presentation.request.AccountBookUpdateRequest;
import com.genesis.unipocket.exchange.query.application.ExchangeRateService;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
@DisplayName("AccountBookCommandService 단위 테스트")
public class AccountBookCommandServiceTest {

	@Mock private AccountBookCommandRepository repository;
	@Mock private UserCommandRepository userRepository;
	@Mock private AccountBookValidator validator;
	@Mock private ExchangeRateService exchangeRateService;

	@InjectMocks private AccountBookCommandService accountBookCommandService;

	private final UUID userId = UUID.randomUUID();
	private final String username = "testUser";

	@Test
	@DisplayName("가계부 생성 - 성공")
	void create_Success() {
		// given
		AccountBookCreateRequest req =
				new AccountBookCreateRequest(
						CountryCode.US, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31));

		CreateAccountBookCommand command = CreateAccountBookCommand.of(userId, username, req);

		UserEntity user = createUser(userId, 0L);
		given(userRepository.findById(userId)).willReturn(Optional.of(user));
		given(repository.findNamesStartingWith(any(), any())).willReturn(Collections.emptyList());
		given(repository.countByUser_Id(userId)).willReturn(0L);
		given(repository.save(any(AccountBookEntity.class)))
				.willAnswer(
						invocation -> {
							AccountBookEntity entity = invocation.getArgument(0);
							// Simulate ID assignment by repository
							try {
								java.lang.reflect.Field idField =
										AccountBookEntity.class.getDeclaredField("id");
								idField.setAccessible(true);
								idField.set(entity, 1L);
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
							return entity;
						});

		// when
		Long resultId = accountBookCommandService.create(command);

		// then
		assertThat(resultId).isEqualTo(1L);
		verify(validator).validate(any(AccountBookEntity.class));
		verify(repository).save(any(AccountBookEntity.class));
		assertThat(user.getMainBucketId()).isEqualTo(1L);
	}

	@Test
	@DisplayName("가계부 생성 - 이름 중복 시 번호 증가")
	void create_DuplicateName() {
		// given
		AccountBookCreateRequest req =
				new AccountBookCreateRequest(
						CountryCode.US, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31));

		CreateAccountBookCommand command = CreateAccountBookCommand.of(userId, username, req);

		String baseTitle = username + "의 가계부";
		UserEntity user = createUser(userId, 1L);
		given(userRepository.findById(userId)).willReturn(Optional.of(user));
		given(repository.findNamesStartingWith(userId, baseTitle))
				.willReturn(List.of(baseTitle + "1", baseTitle + "2"));
		given(repository.countByUser_Id(userId)).willReturn(2L);
		given(repository.findMaxBucketOrderByUserId(userId)).willReturn(2);

		given(repository.save(any(AccountBookEntity.class)))
				.willAnswer(
						invocation -> {
							AccountBookEntity entity = invocation.getArgument(0);
							// Simulate ID assignment
							try {
								java.lang.reflect.Field idField =
										AccountBookEntity.class.getDeclaredField("id");
								idField.setAccessible(true);
								idField.set(entity, 1L);
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
							return entity;
						});

		// when
		Long resultId = accountBookCommandService.create(command);

		// then
		assertThat(resultId).isEqualTo(1L);
		verify(repository).save(any(AccountBookEntity.class));
	}

	@Test
	@DisplayName("가계부 수정 - 성공")
	void update_Success() throws Exception {
		// given
		Long accountBookId = 1L;
		AccountBookUpdateRequest req =
				new AccountBookUpdateRequest(
						"New Title",
						CountryCode.JP,
						CountryCode.KR,
						BigDecimal.valueOf(1000.00),
						LocalDate.of(2023, 2, 1),
						LocalDate.of(2023, 11, 30));

		UpdateAccountBookCommand command = UpdateAccountBookCommand.of(accountBookId, userId, req);

		AccountBookEntity entity =
				AccountBookEntity.create(
						new AccountBookCreateArgs(
								createUser(userId, 1L),
								"Old Title",
								CountryCode.US,
								CountryCode.KR,
								1,
								null,
								LocalDate.of(2023, 1, 1),
								LocalDate.of(2023, 12, 31)));

		// Set ID using reflection
		java.lang.reflect.Field idField = AccountBookEntity.class.getDeclaredField("id");
		idField.setAccessible(true);
		idField.set(entity, accountBookId);

		given(repository.findById(accountBookId)).willReturn(Optional.of(entity));

		// when
		Long resultId = accountBookCommandService.update(command);

		// then
		assertThat(resultId).isEqualTo(accountBookId);
		assertThat(entity.getTitle()).isEqualTo(req.title());
		assertThat(entity.getLocalCountryCode()).isEqualTo(req.localCountryCode());
		verify(validator).validate(entity);
	}

	@Test
	@DisplayName("가계부 수정 - 실패 (존재하지 않음)")
	void update_NotFound() {
		Long accountBookId = 1L;
		AccountBookUpdateRequest req =
				new AccountBookUpdateRequest(
						"Title",
						CountryCode.US,
						CountryCode.KR,
						BigDecimal.valueOf(1000.00),
						LocalDate.now(),
						LocalDate.now());
		UpdateAccountBookCommand command = UpdateAccountBookCommand.of(accountBookId, userId, req);

		given(repository.findById(accountBookId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> accountBookCommandService.update(command))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_BOOK_NOT_FOUND);
	}

	@Test
	@DisplayName("가계부 수정 - 실패 (권한 없음)")
	void update_Unauthorized() {
		Long accountBookId = 1L;
		AccountBookUpdateRequest req =
				new AccountBookUpdateRequest(
						"Title",
						CountryCode.US,
						CountryCode.KR,
						BigDecimal.valueOf(1000.00),
						LocalDate.now(),
						LocalDate.now());
		UpdateAccountBookCommand command = UpdateAccountBookCommand.of(accountBookId, userId, req);

		AccountBookEntity entity =
				AccountBookEntity.create(
						new AccountBookCreateArgs(
								createUser(UUID.randomUUID(), 1L),
								"Title",
								CountryCode.US,
								CountryCode.KR,
								1,
								null,
								LocalDate.now(),
								LocalDate.now()));

		given(repository.findById(accountBookId)).willReturn(Optional.of(entity));

		assertThatThrownBy(() -> accountBookCommandService.update(command))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_BOOK_UNAUTHORIZED_ACCESS);
	}

	@Test
	@DisplayName("가계부 삭제 - 성공")
	void delete_Success() {
		Long accountBookId = 1L;
		DeleteAccountBookCommand command = DeleteAccountBookCommand.of(accountBookId, userId);

		AccountBookEntity entity =
				AccountBookEntity.create(
						new AccountBookCreateArgs(
								createUser(userId, 1L),
								"Title",
								CountryCode.US,
								CountryCode.KR,
								1,
								null,
								LocalDate.now(),
								LocalDate.now()));

		given(repository.findById(accountBookId)).willReturn(Optional.of(entity));

		accountBookCommandService.delete(command);

		verify(repository).delete(entity);
	}

	@Test
	@DisplayName("가계부 수정 - 예산(budget)을 null로 업데이트")
	void update_WithNullBudget_Success() throws Exception {
		// given
		Long accountBookId = 1L;
		AccountBookUpdateRequest req =
				new AccountBookUpdateRequest(
						"New Title",
						CountryCode.JP,
						CountryCode.KR,
						null, // budget is null
						LocalDate.of(2023, 2, 1),
						LocalDate.of(2023, 11, 30));
		UpdateAccountBookCommand command = UpdateAccountBookCommand.of(accountBookId, userId, req);

		AccountBookEntity entity =
				AccountBookEntity.create(
						new AccountBookCreateArgs(
								createUser(userId, 1L),
								"Old Title",
								CountryCode.US,
								CountryCode.KR,
								1,
								BigDecimal.valueOf(1000.00),
								LocalDate.of(2023, 1, 1),
								LocalDate.of(2023, 12, 31)));

		// Set ID using reflection
		java.lang.reflect.Field idField = AccountBookEntity.class.getDeclaredField("id");
		idField.setAccessible(true);
		idField.set(entity, accountBookId);

		given(repository.findById(accountBookId)).willReturn(Optional.of(entity));

		// when
		accountBookCommandService.update(command);

		// then
		assertThat(entity.getBudget()).isNull();
		verify(validator).validate(entity);
	}

	@Test
	@DisplayName("예산 설정 - 환율 반환")
	void updateBudget_ReturnExchangeRate() throws Exception {
		Long accountBookId = 1L;
		AccountBookEntity entity =
				AccountBookEntity.create(
						new AccountBookCreateArgs(
								createUser(userId, 1L),
								"Old Title",
								CountryCode.JP,
								CountryCode.KR,
								1,
								null,
								LocalDate.of(2023, 1, 1),
								LocalDate.of(2023, 12, 31)));
		java.lang.reflect.Field idField = AccountBookEntity.class.getDeclaredField("id");
		idField.setAccessible(true);
		idField.set(entity, accountBookId);

		given(repository.findById(accountBookId)).willReturn(Optional.of(entity));
		given(exchangeRateService.getExchangeRate(any(), any(), any()))
				.willReturn(BigDecimal.valueOf(0.11));

		var result =
				accountBookCommandService.updateBudget(
						accountBookId, userId, BigDecimal.valueOf(1000.00));

		assertThat(result.exchangeRate()).isEqualByComparingTo("0.11");
		assertThat(result.budget()).isEqualByComparingTo("1000.00");
		assertThat(result.budgetCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
	}

	private UserEntity createUser(UUID id, Long mainBucketId) {
		UserEntity user =
				UserEntity.builder()
						.name("tester")
						.email("t@t.com")
						.mainBucketId(mainBucketId)
						.build();
		try {
			java.lang.reflect.Field idField = UserEntity.class.getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(user, id);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return user;
	}
}
