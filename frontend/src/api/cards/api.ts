import { customFetch } from '@/api/config/client';
import { ENDPOINTS } from '@/api/config/endpoint';

import type {
  Card,
  CardCompanies,
  CreateCardRequest,
  UpdateCardNicknameRequest,
} from './type';

export const getCards = (): Promise<Card[]> => {
  return customFetch({
    endpoint: ENDPOINTS.USERS.CARDS,
    options: {
      method: 'GET',
    },
  });
};

export const createCard = (data: CreateCardRequest): Promise<Card> => {
  return customFetch({
    endpoint: ENDPOINTS.USERS.CARDS,
    options: {
      method: 'POST',
      body: JSON.stringify(data),
    },
  });
};

export const deleteCard = (cardId: number): Promise<void> => {
  return customFetch({
    endpoint: ENDPOINTS.USERS.CARD_DETAIL(cardId),
    options: {
      method: 'DELETE',
    },
  });
};

export const updateCardNickname = (
  cardId: number,
  data: UpdateCardNicknameRequest,
): Promise<Card> => {
  return customFetch({
    endpoint: ENDPOINTS.USERS.CARD_DETAIL(cardId),
    options: {
      method: 'PATCH',
      body: JSON.stringify(data),
    },
  });
};

export const getCardCompanies = (): Promise<CardCompanies> => {
  return customFetch({
    endpoint: ENDPOINTS.USERS.CARD_COMPANIES,
    options: {
      method: 'GET',
    },
  });
};
