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

const getAccountBookDetail = (
  accountBookId: number,
): Promise<AccountBookResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.ACCOUNT_BOOKS.DETAIL(accountBookId),
    options: {
      method: 'GET',
    },
  });
};

const deleteAccountBook = (accountBookId: number): Promise<void> => {
  return customFetch({
    endpoint: ENDPOINTS.ACCOUNT_BOOKS.DETAIL(accountBookId),
    options: {
      method: 'DELETE',
    },
  });
};

const updateAccountBook = (
  accountBookId: number,
  data: Partial<CreateAccountBookRequest>,
): Promise<AccountBookResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.ACCOUNT_BOOKS.DETAIL(accountBookId),
    options: {
      method: 'PATCH',
      body: JSON.stringify(data),
    },
  });
};

const updateAccountBookBudget = (accountBookId: number, budget: number) => {
  return customFetch({
    endpoint: ENDPOINTS.ACCOUNT_BOOKS.BUDGET(accountBookId),
    options: {
      method: 'PATCH',
      body: JSON.stringify({ budget }),
    },
  });
};

export {
  createAccountBook,
  deleteAccountBook,
  getAccountBookDetail,
  getAccountBooks,
  updateAccountBook,
  updateAccountBookBudget,
};
