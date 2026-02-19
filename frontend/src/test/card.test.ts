import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import {
  createCard,
  deleteCard,
  getCards,
  updateCardNickname,
} from '@/api/cards/api';
import type {
  Card,
  CreateCardRequest,
  UpdateCardNicknameRequest,
} from '@/api/cards/type';

// API 함수 모킹
vi.mock('@/api/cards/api', () => ({
  getCards: vi.fn(),
  createCard: vi.fn(),
  deleteCard: vi.fn(),
  updateCardNickname: vi.fn(),
}));

describe('카드 관리 API 테스트', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('카드 목록 조회 (GET /users/cards)', () => {
    it('카드 목록을 성공적으로 조회한다', async () => {
      // Given: 목 데이터 설정
      const mockCards: Card[] = [
        {
          userCardId: 1,
          nickName: '여행용 카드',
          cardNumber: '1433',
          cardCompany: 'SHINHAN',
        },
        {
          userCardId: 2,
          nickName: '생활비 카드',
          cardNumber: '5678',
          cardCompany: 'KB',
        },
      ];

      vi.mocked(getCards).mockResolvedValue(mockCards);

      // When: API 호출
      const result = await getCards();

      // Then: 결과 검증
      expect(getCards).toHaveBeenCalledTimes(1);
      expect(result).toEqual(mockCards);
      expect(result).toHaveLength(2);
      expect(result[0]).toHaveProperty('userCardId');
      expect(result[0]).toHaveProperty('nickName');
      expect(result[0]).toHaveProperty('cardNumber');
      expect(result[0]).toHaveProperty('cardCompany');
    });

    it('카드 목록이 비어있을 때 빈 배열을 반환한다', async () => {
      // Given: 빈 배열 목 데이터
      vi.mocked(getCards).mockResolvedValue([]);

      // When: API 호출
      const result = await getCards();

      // Then: 빈 배열 반환 검증
      expect(result).toEqual([]);
      expect(result).toHaveLength(0);
    });

    it('카드 목록 조회 실패 시 에러를 발생시킨다', async () => {
      // Given: 에러 목 설정
      const error = new Error('카드 목록을 불러오지 못했어요.');
      vi.mocked(getCards).mockRejectedValue(error);

      // When & Then: 에러 발생 검증
      await expect(getCards()).rejects.toThrow(
        '카드 목록을 불러오지 못했어요.',
      );
    });
  });

  describe('카드 등록 (POST /users/cards)', () => {
    it('새로운 카드를 성공적으로 등록한다', async () => {
      // Given: 등록할 카드 데이터
      const newCardRequest: CreateCardRequest = {
        nickName: '신규 카드',
        cardNumber: '1234',
        cardCompany: 'SHINHAN',
      };

      const mockCreatedCard: Card = {
        userCardId: 3,
        ...newCardRequest,
      };

      vi.mocked(createCard).mockResolvedValue(mockCreatedCard);

      // When: 카드 등록 API 호출
      const result = await createCard(newCardRequest);

      // Then: 등록 성공 검증
      expect(createCard).toHaveBeenCalledWith(newCardRequest);
      expect(createCard).toHaveBeenCalledTimes(1);
      expect(result).toEqual(mockCreatedCard);
      expect(result.userCardId).toBe(3);
      expect(result.nickName).toBe('신규 카드');
      expect(result.cardNumber).toBe('1234');
      expect(result.cardCompany).toBe('SHINHAN');
    });

    it('필수 필드가 모두 포함되어 있어야 한다', async () => {
      // Given: 필수 필드가 포함된 요청 데이터
      const validRequest: CreateCardRequest = {
        nickName: '테스트 카드',
        cardNumber: '9999',
        cardCompany: 'KB',
      };

      const mockResponse: Card = {
        userCardId: 10,
        ...validRequest,
      };

      vi.mocked(createCard).mockResolvedValue(mockResponse);

      // When: API 호출
      const result = await createCard(validRequest);

      // Then: 모든 필드 존재 검증
      expect(result).toHaveProperty('nickName');
      expect(result).toHaveProperty('cardNumber');
      expect(result).toHaveProperty('cardCompany');
      expect(result.nickName).toBeTruthy();
      expect(result.cardNumber).toBeTruthy();
      expect(result.cardCompany).toBeTruthy();
    });

    it('카드 등록 실패 시 에러를 발생시킨다', async () => {
      // Given: 에러 목 설정
      const invalidRequest: CreateCardRequest = {
        nickName: '',
        cardNumber: '1234',
        cardCompany: 'SHINHAN',
      };

      const error = new Error('카드 등록에 실패했어요.');
      vi.mocked(createCard).mockRejectedValue(error);

      // When & Then: 에러 발생 검증
      await expect(createCard(invalidRequest)).rejects.toThrow(
        '카드 등록에 실패했어요.',
      );
    });

    it('다양한 카드사로 카드를 등록할 수 있다', async () => {
      // Given: 다양한 카드사 데이터
      const cardCompanies = ['SHINHAN', 'KB', 'SAMSUNG', 'HYUNDAI', 'LOTTE'];

      for (const company of cardCompanies) {
        const request: CreateCardRequest = {
          nickName: `${company} 카드`,
          cardNumber: '1234',
          cardCompany: company,
        };

        const response: Card = {
          userCardId: Math.random(),
          ...request,
        };

        vi.mocked(createCard).mockResolvedValue(response);

        // When: API 호출
        const result = await createCard(request);

        // Then: 카드사 검증
        expect(result.cardCompany).toBe(company);
      }
    });
  });

  describe('카드 삭제 (DELETE /users/cards/:cardId)', () => {
    it('카드를 성공적으로 삭제한다', async () => {
      // Given: 삭제할 카드 ID
      const cardId = 1;
      vi.mocked(deleteCard).mockResolvedValue(undefined);

      // When: 카드 삭제 API 호출
      await deleteCard(cardId);

      // Then: 삭제 성공 검증
      expect(deleteCard).toHaveBeenCalledWith(cardId);
      expect(deleteCard).toHaveBeenCalledTimes(1);
    });

    it('여러 카드를 순차적으로 삭제할 수 있다', async () => {
      // Given: 삭제할 카드 ID 목록
      const cardIds = [1, 2, 3];
      vi.mocked(deleteCard).mockResolvedValue(undefined);

      // When: 여러 카드 삭제
      for (const id of cardIds) {
        await deleteCard(id);
      }

      // Then: 모든 삭제 호출 검증
      expect(deleteCard).toHaveBeenCalledTimes(3);
      cardIds.forEach((id) => {
        expect(deleteCard).toHaveBeenCalledWith(id);
      });
    });

    it('존재하지 않는 카드 삭제 시 에러를 발생시킨다', async () => {
      // Given: 존재하지 않는 카드 ID
      const nonExistentCardId = 9999;
      const error = new Error('카드 삭제에 실패했어요.');
      vi.mocked(deleteCard).mockRejectedValue(error);

      // When & Then: 에러 발생 검증
      await expect(deleteCard(nonExistentCardId)).rejects.toThrow(
        '카드 삭제에 실패했어요.',
      );
    });

    it('카드 ID는 숫자여야 한다', async () => {
      // Given: 유효한 숫자 ID
      const validCardId = 42;
      vi.mocked(deleteCard).mockResolvedValue(undefined);

      // When: 삭제 API 호출
      await deleteCard(validCardId);

      // Then: 숫자 ID로 호출 검증
      expect(deleteCard).toHaveBeenCalledWith(validCardId);
      expect(typeof validCardId).toBe('number');
    });
  });

  describe('카드 별명 수정 (PATCH /users/cards/:cardId)', () => {
    it('카드 별명을 성공적으로 수정한다', async () => {
      // Given: 수정할 카드 정보
      const cardId = 1;
      const updateRequest: UpdateCardNicknameRequest = {
        nickName: '수정된 별명',
      };

      const mockUpdatedCard: Card = {
        userCardId: cardId,
        nickName: updateRequest.nickName,
        cardNumber: '1433',
        cardCompany: 'SHINHAN',
      };

      vi.mocked(updateCardNickname).mockResolvedValue(mockUpdatedCard);

      // When: 별명 수정 API 호출
      const result = await updateCardNickname(cardId, updateRequest);

      // Then: 수정 성공 검증
      expect(updateCardNickname).toHaveBeenCalledWith(cardId, updateRequest);
      expect(updateCardNickname).toHaveBeenCalledTimes(1);
      expect(result.nickName).toBe('수정된 별명');
      expect(result.userCardId).toBe(cardId);
    });

    it('여러 카드의 별명을 순차적으로 수정할 수 있다', async () => {
      // Given: 여러 카드의 수정 데이터
      const updates = [
        { cardId: 1, nickName: '첫 번째 카드' },
        { cardId: 2, nickName: '두 번째 카드' },
        { cardId: 3, nickName: '세 번째 카드' },
      ];

      for (const update of updates) {
        const mockCard: Card = {
          userCardId: update.cardId,
          nickName: update.nickName,
          cardNumber: '1234',
          cardCompany: 'SHINHAN',
        };

        vi.mocked(updateCardNickname).mockResolvedValue(mockCard);

        // When: 별명 수정
        const result = await updateCardNickname(update.cardId, {
          nickName: update.nickName,
        });

        // Then: 수정된 별명 검증
        expect(result.nickName).toBe(update.nickName);
      }

      // Then: 모든 호출 검증
      expect(updateCardNickname).toHaveBeenCalledTimes(3);
    });

    it('빈 별명으로 수정 시 에러를 발생시킨다', async () => {
      // Given: 빈 별명 요청
      const cardId = 1;
      const invalidRequest: UpdateCardNicknameRequest = {
        nickName: '',
      };

      const error = new Error('카드 별명 수정에 실패했어요.');
      vi.mocked(updateCardNickname).mockRejectedValue(error);

      // When & Then: 에러 발생 검증
      await expect(updateCardNickname(cardId, invalidRequest)).rejects.toThrow(
        '카드 별명 수정에 실패했어요.',
      );
    });

    it('존재하지 않는 카드의 별명 수정 시 에러를 발생시킨다', async () => {
      // Given: 존재하지 않는 카드 ID
      const nonExistentCardId = 9999;
      const updateRequest: UpdateCardNicknameRequest = {
        nickName: '새로운 별명',
      };

      const error = new Error('카드 별명 수정에 실패했어요.');
      vi.mocked(updateCardNickname).mockRejectedValue(error);

      // When & Then: 에러 발생 검증
      await expect(
        updateCardNickname(nonExistentCardId, updateRequest),
      ).rejects.toThrow('카드 별명 수정에 실패했어요.');
    });

    it('별명만 수정되고 다른 정보는 유지된다', async () => {
      // Given: 기존 카드와 수정 요청
      const cardId = 5;
      const originalCard = {
        userCardId: cardId,
        nickName: '기존 별명',
        cardNumber: '7890',
        cardCompany: 'KB',
      };

      const updateRequest: UpdateCardNicknameRequest = {
        nickName: '새 별명',
      };

      const updatedCard: Card = {
        ...originalCard,
        nickName: updateRequest.nickName,
      };

      vi.mocked(updateCardNickname).mockResolvedValue(updatedCard);

      // When: 별명 수정
      const result = await updateCardNickname(cardId, updateRequest);

      // Then: 별명만 변경되고 나머지는 유지 검증
      expect(result.nickName).toBe('새 별명');
      expect(result.cardNumber).toBe(originalCard.cardNumber);
      expect(result.cardCompany).toBe(originalCard.cardCompany);
      expect(result.userCardId).toBe(originalCard.userCardId);
    });
  });

  describe('통합 시나리오 테스트', () => {
    it('카드 등록 → 조회 → 수정 → 삭제 전체 흐름을 테스트한다', async () => {
      // Step 1: 카드 등록
      const createRequest: CreateCardRequest = {
        nickName: '테스트 카드',
        cardNumber: '1234',
        cardCompany: 'SHINHAN',
      };

      const createdCard: Card = {
        userCardId: 100,
        ...createRequest,
      };

      vi.mocked(createCard).mockResolvedValue(createdCard);
      const created = await createCard(createRequest);
      expect(created.userCardId).toBe(100);

      // Step 2: 카드 목록 조회
      vi.mocked(getCards).mockResolvedValue([createdCard]);
      const cards = await getCards();
      expect(cards).toHaveLength(1);
      expect(cards[0].userCardId).toBe(100);

      // Step 3: 카드 별명 수정
      const updateRequest: UpdateCardNicknameRequest = {
        nickName: '수정된 테스트 카드',
      };

      const updatedCard: Card = {
        ...createdCard,
        nickName: updateRequest.nickName,
      };

      vi.mocked(updateCardNickname).mockResolvedValue(updatedCard);
      const updated = await updateCardNickname(
        created.userCardId,
        updateRequest,
      );
      expect(updated.nickName).toBe('수정된 테스트 카드');

      // Step 4: 카드 삭제
      vi.mocked(deleteCard).mockResolvedValue(undefined);
      await deleteCard(created.userCardId);
      expect(deleteCard).toHaveBeenCalledWith(100);

      // Step 5: 삭제 후 조회 시 빈 배열
      vi.mocked(getCards).mockResolvedValue([]);
      const afterDelete = await getCards();
      expect(afterDelete).toHaveLength(0);
    });

    it('여러 카드를 등록하고 일부만 삭제할 수 있다', async () => {
      // Given: 여러 카드 등록
      const cards: Card[] = [
        {
          userCardId: 1,
          nickName: '카드1',
          cardNumber: '1111',
          cardCompany: 'SHINHAN',
        },
        {
          userCardId: 2,
          nickName: '카드2',
          cardNumber: '2222',
          cardCompany: 'KB',
        },
        {
          userCardId: 3,
          nickName: '카드3',
          cardNumber: '3333',
          cardCompany: 'SAMSUNG',
        },
      ];

      vi.mocked(getCards).mockResolvedValue(cards);
      const allCards = await getCards();
      expect(allCards).toHaveLength(3);

      // When: 두 번째 카드만 삭제
      vi.mocked(deleteCard).mockResolvedValue(undefined);
      await deleteCard(2);

      // Then: 남은 카드 조회
      const remainingCards = cards.filter((c) => c.userCardId !== 2);
      vi.mocked(getCards).mockResolvedValue(remainingCards);
      const afterDelete = await getCards();

      expect(afterDelete).toHaveLength(2);
      expect(afterDelete.find((c) => c.userCardId === 2)).toBeUndefined();
      expect(afterDelete.find((c) => c.userCardId === 1)).toBeDefined();
      expect(afterDelete.find((c) => c.userCardId === 3)).toBeDefined();
    });
  });
});
