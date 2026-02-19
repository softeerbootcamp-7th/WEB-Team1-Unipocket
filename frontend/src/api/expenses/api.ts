import { customFetch } from '@/api/config/client';
import { ENDPOINTS } from '@/api/config/endpoint';
import {
  type CreateManualExpenseRequest,
  type CreateManualExpenseResponse,
  type ExpenseSearchFilter,
  type GetExpenseDetailResponse,
  type GetExpenseFileUrlResponse,
  type GetExpensesResponse,
  type SearchMerchantNamesResponse,
  type UpdateExpenseRequest,
  type UpdateExpenseResponse,
} from '@/api/expenses/type';

/** 특정 지출 상세 조회 */
const getExpenseDetail = (
  accountBookId: number | string,
  expenseId: number | string,
): Promise<GetExpenseDetailResponse> => {
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
  data: UpdateExpenseRequest,
): Promise<UpdateExpenseResponse> => {
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

const createManualExpense = (
  accountBookId: number | string,
  data: CreateManualExpenseRequest,
): Promise<CreateManualExpenseResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.EXPENSES.MANUAL_UPLOAD(accountBookId),
    options: {
      method: 'POST',
      body: JSON.stringify(data),
    },
  });
};

const getExpenses = (
  accountBookId: number | string,
  filter?: ExpenseSearchFilter,
): Promise<GetExpensesResponse> => {
  const params: Record<string, string> = {};

  if (filter) {
    if (filter.startDate) params.startDate = filter.startDate;
    if (filter.endDate) params.endDate = filter.endDate;
    if (filter.merchantName) params.merchantName = filter.merchantName;
    if (filter.category !== undefined && filter.category !== null) {
      params.category = filter.category.toString();
    }
    if (filter.travelId !== undefined && filter.travelId !== null) {
      params.travelId = filter.travelId.toString();
    }
    if (filter.minAmount !== undefined && filter.minAmount !== null) {
      params.minAmount = filter.minAmount.toString();
    }
    if (filter.maxAmount !== undefined && filter.maxAmount !== null) {
      params.maxAmount = filter.maxAmount.toString();
    }
    if (filter.page !== undefined) {
      params.page = filter.page.toString();
    }
    if (filter.size !== undefined) {
      params.size = filter.size.toString();
    }
    if (filter.sort && filter.sort.length > 0) {
      params.sort = filter.sort.join(',');
    }
  }
  return customFetch({
    endpoint: ENDPOINTS.EXPENSES.BASE(accountBookId),
    params: Object.keys(params).length > 0 ? params : undefined,
    options: {
      method: 'GET',
    },
  });
};

const getExpenseFileUrl = (
  accountBookId: number | string,
  expenseId: number | string,
): Promise<GetExpenseFileUrlResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.EXPENSES.FILE_URL(accountBookId, expenseId),
    options: {
      method: 'GET',
    },
  });
};

/** 거래처명 자동완성 검색 */
const searchMerchantNames = (
  accountBookId: number | string,
  query: string,
  limit?: number,
): Promise<SearchMerchantNamesResponse> => {
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
  createManualExpense,
  deleteExpense,
  getExpenseDetail,
  getExpenseFileUrl,
  getExpenses,
  searchMerchantNames,
  updateExpense,
};
