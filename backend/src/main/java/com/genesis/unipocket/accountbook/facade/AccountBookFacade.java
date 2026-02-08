package com.genesis.unipocket.accountbook.facade;

import com.genesis.unipocket.accountbook.dto.common.AccountBookDto;
import com.genesis.unipocket.accountbook.dto.converter.AccountBookDtoConverter;
import com.genesis.unipocket.accountbook.dto.request.AccountBookCreateRequest;
import com.genesis.unipocket.accountbook.dto.request.AccountBookUpdateRequest;
import com.genesis.unipocket.accountbook.dto.response.AccountBookDetailResponse;
import com.genesis.unipocket.accountbook.dto.response.AccountBookResponse;
import com.genesis.unipocket.accountbook.dto.response.AccountBookSummaryResponse;
import com.genesis.unipocket.accountbook.service.AccountBookService;
import com.genesis.unipocket.user.dto.response.UserResponse;
import com.genesis.unipocket.user.service.UserService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountBookFacade {

	private final AccountBookService accountBookService;
	private final AccountBookDtoConverter converter;
	private final UserService userService;

	public AccountBookResponse createAccountBook(UUID userId, AccountBookCreateRequest req) {

		String userIdStr = userId.toString();
		UserResponse userResponse = userService.getUserInfo(userId);

		AccountBookDto dto = accountBookService.create(userIdStr, userResponse.name(), req);

		return converter.toResponse(dto);
	}

	public AccountBookResponse updateAccountBook(
			UUID userId, Long accountBookId, AccountBookUpdateRequest req) {

		String userIdStr = userId.toString();

		AccountBookDto dto = accountBookService.update(accountBookId, userIdStr, req);

		return converter.toResponse(dto);
	}

	public void deleteAccountBook(UUID userId, Long accountBookId) {
		String userIdStr = userId.toString();
		accountBookService.delete(accountBookId, userIdStr);
	}

	public AccountBookDetailResponse getAccountBook(UUID userId, Long accountBookId) {
		String userIdStr = userId.toString();

		AccountBookDto dto = accountBookService.getAccountBook(accountBookId, userIdStr);

		// TODO: 임시 지출 내역 리스트 받아오기

		return converter.toDetailResponse(dto, List.of());
	}

	public List<AccountBookSummaryResponse> getAccountBooks(UUID userId) {
		UserResponse userResponse = userService.getUserInfo(userId);

		// TODO: 유저 정보 받아와서 메인 가계부인지 확인하기

		return accountBookService.getAccountBooks(userId.toString()).stream()
				.map((accountBookDto) -> converter.toSummaryResponse(accountBookDto, 1L))
				.toList();
	}
}
