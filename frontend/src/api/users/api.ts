import { customFetch } from '@/api/config/client';
import { ENDPOINTS } from '@/api/config/endpoint';
import type {
  CreateCardRequest,
  GetCardCompaniesResponse,
  GetCardsResponse,
  GetUserResponse,
} from '@/api/users/type';

// USER
const getUser = (): Promise<GetUserResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.USERS.BASE,
    options: {
      method: 'GET',
    },
  });
};

const deleteUser = (): Promise<void> => {
  return customFetch({
    endpoint: ENDPOINTS.USERS.BASE,
    options: {
      method: 'DELETE',
    },
  });
};

// CARD
const getCards = (): Promise<GetCardsResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.USERS.CARDS,
    options: {
      method: 'GET',
    },
  });
};

const createCard = (data: CreateCardRequest): Promise<void> => {
  return customFetch({
    endpoint: ENDPOINTS.USERS.CARDS,
    options: {
      method: 'POST',
      body: JSON.stringify(data),
    },
  });
};

const getCardCompanies = (): Promise<GetCardCompaniesResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.USERS.CARD_COMPANIES,
    options: {
      method: 'GET',
    },
  });
};

const deleteCard = (cardId: number): Promise<void> => {
  return customFetch({
    endpoint: ENDPOINTS.USERS.CARD_DETAIL(cardId),
    options: {
      method: 'DELETE',
    },
  });
};

export {
  createCard,
  deleteCard,
  deleteUser,
  getCardCompanies,
  getCards,
  getUser,
};
