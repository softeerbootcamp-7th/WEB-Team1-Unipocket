import { customFetch } from '@/api/config/client';
import { ENDPOINTS } from '@/api/config/endpoint';
import {
  type CreateExpenseRequest,
  type ExpenseResponse,
  type ExpenseSearchFilter,
  type GetExpensesResponse,
  type MerchantNamesResponse,
} from '@/api/expenses/type';

/** 특정 가계부의 지출 내역 목록 조회 (필터링, 페이징 지원) */
const getExpenses = (
  accountBookId: number | string,
  filter?: ExpenseSearchFilter,
): Promise<GetExpensesResponse> => {
  // filter를 params로 변환 (모든 값을 string으로)
  const params: Record<string, string> = {};

  if (filter) {
    if (filter.startDate) params.startDate = filter.startDate;
    if (filter.endDate) params.endDate = filter.endDate;
    if (filter.category) params.category = filter.category.toString();
    if (filter.minAmount !== undefined)
      params.minAmount = filter.minAmount.toString();
    if (filter.maxAmount !== undefined)
      params.maxAmount = filter.maxAmount.toString();
    if (filter.merchantName) params.merchantName = filter.merchantName;
    if (filter.travelId !== undefined)
      params.travelId = filter.travelId.toString();
    if (filter.page !== undefined) params.page = filter.page.toString();
    if (filter.size !== undefined) params.size = filter.size.toString();
    if (filter.sort) params.sort = filter.sort;
  }

  return customFetch({
    endpoint: ENDPOINTS.EXPENSES.BASE(accountBookId),
    params: Object.keys(params).length > 0 ? params : undefined,
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
  data: CreateExpenseRequest,
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

/** 거래처명 자동완성 검색 */
const searchMerchantNames = (
  accountBookId: number | string,
  query: string,
  limit?: number,
): Promise<MerchantNamesResponse> => {
  const params: Record<string, string> = { q: query };
  if (limit !== undefined) {
    params.limit = limit.toString();
  }

  return customFetch({
    endpoint: ENDPOINTS.EXPENSES.MERCHANT_SEARCH(accountBookId),
    params,
    options: {
      method: 'GET',
    },
  });
};

export {
  createExpense,
  deleteExpense,
  getExpenseDetail,
  getExpenses,
  searchMerchantNames,
  updateExpense,
};
