import {
  type AccountBookResponse,
  type CreateAccountBookRequest,
  type GetAccountBooksResponse,
} from '@/api/account-books/type';
import { customFetch } from '@/api/config/client';
import { ENDPOINTS } from '@/api/config/endpoint';

const createAccountBook = (
  data: CreateAccountBookRequest,
): Promise<AccountBookResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.ACCOUNT_BOOKS.BASE,
    options: {
      method: 'POST',
      body: JSON.stringify(data),
    },
  });
};

const getAccountBooks = (): Promise<GetAccountBooksResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.ACCOUNT_BOOKS.BASE,
    options: {
      method: 'GET',
    },
  });
};

export { createAccountBook, getAccountBooks };
