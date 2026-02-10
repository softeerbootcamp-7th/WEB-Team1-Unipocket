package com.genesis.unipocket.accountbook.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.genesis.unipocket.accountbook.query.persistence.repository.AccountBookQueryRepository;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookQueryResponse;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookSummaryResponse;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.time.LocalDate;
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
@DisplayName("AccountBookQueryService 단위 테스트")
class AccountBookQueryServiceTest {

    @Mock
    private AccountBookQueryRepository repository;

    @InjectMocks
    private AccountBookQueryService accountBookQueryService;

    private final String userId = UUID.randomUUID().toString();

    @Test
    @DisplayName("가계부 조회 - 성공")
    void getAccountBook_Success() {
        // given
        Long accountBookId = 1L;
        AccountBookQueryResponse response = new AccountBookQueryResponse(
                accountBookId,
                "Title",
                CountryCode.US,
                CountryCode.KR,
                LocalDate.now(),
                LocalDate.now());

        given(repository.findById(accountBookId)).willReturn(Optional.of(response));

        // when
        AccountBookQueryResponse result = accountBookQueryService.getAccountBook(accountBookId);

        // then
        assertThat(result.id()).isEqualTo(accountBookId);
        assertThat(result.title()).isEqualTo("Title");
    }

    @Test
    @DisplayName("가계부 조회 - 실패 (존재하지 않음)")
    void getAccountBook_NotFound() {
        // given
        Long accountBookId = 1L;
        given(repository.findById(accountBookId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accountBookQueryService.getAccountBook(accountBookId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_BOOK_NOT_FOUND);
    }

    @Test
    @DisplayName("내 가계부 목록 조회 - 성공")
    void getAccountBooks_Success() {
        // given
        AccountBookSummaryResponse response1 = new AccountBookSummaryResponse(1L, "Title1", true);
        AccountBookSummaryResponse response2 = new AccountBookSummaryResponse(2L, "Title2", false);

        given(repository.findAllByUserId(userId, 1L)).willReturn(List.of(response1, response2));

        // when
        List<AccountBookSummaryResponse> result = accountBookQueryService.getAccountBooks(userId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("Title1");
    }
}
