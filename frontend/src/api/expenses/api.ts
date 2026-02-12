import { customFetch } from '@/api/config/client';
import { ENDPOINTS } from '@/api/config/endpoint';
import {
  type CreateExpenseRequest,
  type ExpenseResponse,
  type GetExpensesResponse,
} from '@/api/expenses/type';

/** 특정 가계부의 지출 내역 목록 조회 */
const getExpenses = (
  accountBookId: number | string,
): Promise<GetExpensesResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.EXPENSES.BASE(accountBookId),
    options: {
      method: 'GET',
    },
  });
};

/** 지출 내역 수동 등록 */
const createExpense = (
  accountBookId: number | string,
  data: CreateExpenseRequest,
): Promise<ExpenseResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.EXPENSES.MANUAL_UPLOAD(accountBookId),
    options: {
      method: 'POST',
      body: JSON.stringify(data),
    },
  });
};

/** 특정 지출 상세 조회 */
const getExpenseDetail = (
  accountBookId: number | string,
  expenseId: number | string,
): Promise<ExpenseResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.EXPENSES.DETAIL(accountBookId, expenseId),
    options: {
      method: 'GET',
    },
  });
};

/** 지출 내역 수정 */
const updateExpense = (
  accountBookId: number | string,
  expenseId: number | string,
  data: Partial<CreateExpenseRequest>,
): Promise<ExpenseResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.EXPENSES.DETAIL(accountBookId, expenseId),
    options: {
      method: 'PUT',
      body: JSON.stringify(data),
    },
  });
};

/** 지출 내역 삭제 */
const deleteExpense = (
  accountBookId: number | string,
  expenseId: number | string,
): Promise<void> => {
  return customFetch({
    endpoint: ENDPOINTS.EXPENSES.DETAIL(accountBookId, expenseId),
    options: {
      method: 'DELETE',
    },
  });
};

export {
  createExpense,
  deleteExpense,
  getExpenseDetail,
  getExpenses,
  updateExpense,
};
