import {
  type AccountBookResponse,
  type CreateAccountBookRequest,
} from '@/api/account-books/type';
import { customFetch } from '@/api/config/client';
import { ENDPOINTS } from '@/api/config/endpoint';

export const createAccountBook = async (
  data: CreateAccountBookRequest,
): Promise<AccountBookResponse> => {
  return customFetch(ENDPOINTS.ACCOUNT_BOOKS, {
    method: 'POST',
    body: JSON.stringify(data),
  });
};
