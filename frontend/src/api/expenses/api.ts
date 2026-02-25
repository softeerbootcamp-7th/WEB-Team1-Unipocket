import { customFetch } from '@/api/config/client';
import { ENDPOINTS } from '@/api/config/endpoint';
import {
  type BulkUpdateExpenseRequest,
  type BulkUpdateExpenseResponse,
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

/** 지출 내역 일괄 수정 */
const bulkUpdateExpenses = (
  accountBookId: number | string,
  data: BulkUpdateExpenseRequest,
): Promise<BulkUpdateExpenseResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.EXPENSES.BULK(accountBookId),
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
  const params: Record<string, string | string[]> = {};

  if (filter) {
    if (filter.startDate) params.startDate = filter.startDate;
    if (filter.endDate) params.endDate = filter.endDate;
    if (filter.merchantName) params.merchantName = filter.merchantName;
    if (filter.category && filter.category.length > 0) {
      params.category = filter.category.map((id) => id.toString());
    }
    if (filter.travelId) params.travelId = filter.travelId.toString();
    if (filter.page !== undefined) {
      params.page = filter.page.toString();
    }
    if (filter.size !== undefined) {
      params.size = filter.size.toString();
    }
    if (filter.sort && filter.sort.length > 0) {
      params.sort = filter.sort.join(',');
    }
    if (filter.cardNumber && filter.cardNumber.length > 0) {
      params.cardNumber = filter.cardNumber;
    }
    if (filter.isCash !== undefined) {
      params.isCash = filter.isCash.toString();
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
  bulkUpdateExpenses,
  createManualExpense,
  deleteExpense,
  getExpenseDetail,
  getExpenseFileUrl,
  getExpenses,
  searchMerchantNames,
  updateExpense,
};
