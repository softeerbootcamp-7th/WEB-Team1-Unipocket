import {
  type AccountBookResponse,
  type CreateAccountBookRequest,
} from '@/api/account-books/type';
import { customFetch } from '@/api/config/client';
import { ENDPOINTS } from '@/api/config/endpoint';

export const createAccountBook = (data: CreateAccountBookRequest) => {
  return customFetch<AccountBookResponse>({
    endpoint: ENDPOINTS.ACCOUNT_BOOKS,
    options: {
      method: 'POST',
      body: JSON.stringify(data),
    },
  });
};
