import type {
  AccountBookDetail,
  AccountBookResponse,
  AccountBookSummary,
  CreateAccountBookRequest,
  UpdateAccountBookRequest,
} from '@/api/account-books/type';
import { customFetch } from '@/api/config/client';
import { ENDPOINTS } from '@/api/config/endpoint';

export const createAccountBook = (
  data: CreateAccountBookRequest,
): Promise<AccountBookResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.ACCOUNT_BOOKS,
    options: {
      method: 'POST',
      body: JSON.stringify(data),
    },
  });
};

export const getAccountBooks = (): Promise<AccountBookSummary[]> => {
  return customFetch({
    endpoint: ENDPOINTS.ACCOUNT_BOOKS,
    options: {
      method: 'GET',
    },
  });
};

export const getAccountBookDetail = (
  accountBookId: number,
): Promise<AccountBookDetail> => {
  return customFetch({
    endpoint: `${ENDPOINTS.ACCOUNT_BOOKS}/${accountBookId}`,
    options: {
      method: 'GET',
    },
  });
};

export const updateAccountBook = (
  accountBookId: number,
  data: UpdateAccountBookRequest,
): Promise<AccountBookDetail> => {
  return customFetch({
    endpoint: `${ENDPOINTS.ACCOUNT_BOOKS}/${accountBookId}`,
    options: {
      method: 'PATCH',
      body: JSON.stringify(data),
    },
  });
};

export const deleteAccountBook = (accountBookId: number): Promise<void> => {
  return customFetch({
    endpoint: `${ENDPOINTS.ACCOUNT_BOOKS}/${accountBookId}`,
    options: {
      method: 'DELETE',
    },
  });
};

export const setMainAccountBook = (accountBookId: number): Promise<void> => {
  return customFetch({
    endpoint: `${ENDPOINTS.ACCOUNT_BOOKS}/${accountBookId}/main`,
    options: {
      method: 'PATCH',
    },
  });
};
