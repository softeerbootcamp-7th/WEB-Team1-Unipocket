import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import {
  createAccountBook,
  deleteAccountBook,
  getAccountBooks,
  updateAccountBook,
} from '@/api/account-books/api';
import type {
  CreateAccountBookRequest,
  CreateAccountBookResponse,
  GetAccountBookDetailResponse,
  GetAccountBooksResponse,
  UpdateAccountBookRequest,
} from '@/api/account-books/type';
import { ApiError } from '@/api/config/error';

/** fetch 응답을 흉내 내는 헬퍼 */
const createMockResponse = (status: number, body?: unknown): Response =>
  ({
    ok: status >= 200 && status < 300,
    status,
    json: vi.fn().mockResolvedValue(body ?? null),
  }) as unknown as Response;

describe('Account Books API', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  // ─────────────────────────────────────────────────────────────
  // GET /account-books  (가계부 목록 조회)
  // ─────────────────────────────────────────────────────────────
  describe('getAccountBooks', () => {
    it('가계부 목록을 정상적으로 반환한다', async () => {
      const mockData: GetAccountBooksResponse = [
        { accountBookId: 1, title: '유럽 여행 가계부', isMain: true },
        { accountBookId: 2, title: '일본 여행 가계부', isMain: false },
      ];
      vi.mocked(fetch).mockResolvedValue(createMockResponse(200, mockData));

      const result = await getAccountBooks();

      expect(result).toEqual(mockData);
      expect(fetch).toHaveBeenCalledTimes(1);
    });

    it('가계부가 없으면 빈 배열을 반환한다', async () => {
      vi.mocked(fetch).mockResolvedValue(createMockResponse(200, []));

      const result = await getAccountBooks();

      expect(result).toEqual([]);
    });

    it('서버 오류(500) 시 ApiError를 던진다', async () => {
      vi.mocked(fetch).mockResolvedValue(createMockResponse(500));

      const error = await getAccountBooks().catch((e) => e);

      expect(error).toBeInstanceOf(ApiError);
      expect((error as ApiError).status).toBe(500);
    });
  });

  // ─────────────────────────────────────────────────────────────
  // POST /account-books  (가계부 생성)
  // ─────────────────────────────────────────────────────────────
  describe('createAccountBook', () => {
    const createRequest: CreateAccountBookRequest = {
      localCountryCode: 'KR',
      startDate: '2026-02-19',
      endDate: '2026-02-25',
    };

    const mockCreatedBook: CreateAccountBookResponse = {
      accountBookId: 1,
      title: '한국 여행',
      localCountryCode: 'KR',
      baseCountryCode: 'KR',
      startDate: '2026-02-19',
      endDate: '2026-02-25',
    };

    it('가계부를 생성하고 생성된 가계부 정보를 반환한다', async () => {
      vi.mocked(fetch).mockResolvedValue(
        createMockResponse(200, mockCreatedBook),
      );

      const result = await createAccountBook(createRequest);

      expect(result).toEqual(mockCreatedBook);
      expect(fetch).toHaveBeenCalledTimes(1);
      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('account-books'),
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify(createRequest),
        }),
      );
    });

    it('잘못된 요청(400) 시 ApiError를 던진다', async () => {
      vi.mocked(fetch).mockResolvedValue(
        createMockResponse(400, { message: '잘못된 요청입니다.' }),
      );

      const error = await createAccountBook(createRequest).catch((e) => e);

      expect(error).toBeInstanceOf(ApiError);
      expect((error as ApiError).status).toBe(400);
    });
  });

  // ─────────────────────────────────────────────────────────────
  // PATCH /account-books/{accountBookId}  (가계부 수정)
  // ─────────────────────────────────────────────────────────────
  describe('updateAccountBook', () => {
    const accountBookId = 1;

    const updateRequest: UpdateAccountBookRequest = {
      title: '수정된 가계부',
      localCountryCode: 'JP',
      startDate: '2026-03-01',
      endDate: '2026-03-10',
      isMain: true,
    };

    const mockUpdatedBook: GetAccountBookDetailResponse = {
      accountBookId: 1,
      title: '수정된 가계부',
      localCountryCode: 'JP',
      baseCountryCode: 'KR',
      startDate: '2026-03-01',
      endDate: '2026-03-10',
    };

    it('가계부 정보를 수정하고 수정된 정보를 반환한다', async () => {
      vi.mocked(fetch).mockResolvedValue(
        createMockResponse(200, mockUpdatedBook),
      );

      const result = await updateAccountBook(accountBookId, updateRequest);

      expect(result).toEqual(mockUpdatedBook);
      expect(fetch).toHaveBeenCalledTimes(1);
      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining(`account-books/${accountBookId}`),
        expect.objectContaining({
          method: 'PATCH',
          body: JSON.stringify(updateRequest),
        }),
      );
    });

    it('일부 필드만 수정할 수 있다 (부분 업데이트)', async () => {
      const partialRequest: UpdateAccountBookRequest = {
        title: '제목만 변경',
        isMain: true,
      };
      const mockPartialUpdated: GetAccountBookDetailResponse = {
        accountBookId: 1,
        title: '제목만 변경',
        localCountryCode: 'KR',
        baseCountryCode: 'KR',
        startDate: '2026-02-19',
        endDate: '2026-02-25',
      };

      vi.mocked(fetch).mockResolvedValue(
        createMockResponse(200, mockPartialUpdated),
      );

      const result = await updateAccountBook(accountBookId, partialRequest);

      expect(result).toEqual(mockPartialUpdated);
      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining(`account-books/${accountBookId}`),
        expect.objectContaining({
          method: 'PATCH',
          body: JSON.stringify(partialRequest),
        }),
      );
    });

    it('존재하지 않는 가계부 수정 시 ApiError(404)를 던진다', async () => {
      vi.mocked(fetch).mockResolvedValue(createMockResponse(404));

      const error = await updateAccountBook(accountBookId, updateRequest).catch(
        (e) => e,
      );

      expect(error).toBeInstanceOf(ApiError);
      expect((error as ApiError).status).toBe(404);
    });
  });

  // ─────────────────────────────────────────────────────────────
  // DELETE /account-books/{accountBookId}  (가계부 삭제)
  // ─────────────────────────────────────────────────────────────
  describe('deleteAccountBook', () => {
    const accountBookId = 1;

    it('가계부를 성공적으로 삭제한다 (204 No Content)', async () => {
      vi.mocked(fetch).mockResolvedValue(createMockResponse(204));

      await expect(deleteAccountBook(accountBookId)).resolves.toBeUndefined();
      expect(fetch).toHaveBeenCalledTimes(1);
      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining(`account-books/${accountBookId}`),
        expect.objectContaining({ method: 'DELETE' }),
      );
    });

    it('존재하지 않는 가계부 삭제 시 ApiError(404)를 던진다', async () => {
      vi.mocked(fetch).mockResolvedValue(createMockResponse(404));

      const error = await deleteAccountBook(accountBookId).catch((e) => e);

      expect(error).toBeInstanceOf(ApiError);
      expect((error as ApiError).status).toBe(404);
    });
  });
});
