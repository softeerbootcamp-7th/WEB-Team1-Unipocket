import {
  useMutation,
  type UseMutationOptions,
  useQuery,
  type UseQueryOptions,
  useSuspenseQuery,
} from '@tanstack/react-query';

import {
  createExpense,
  deleteExpense,
  getExpenseDetail,
  getExpenses,
  searchMerchantNames,
  updateExpense,
} from '@/api/expenses/api';
import type {
  CreateExpenseRequest,
  ExpenseResponse,
  ExpenseSearchFilter,
  GetExpensesResponse,
  MerchantNamesResponse,
} from '@/api/expenses/type';

/** 지출 내역 목록 조회 Query */
export const useGetExpensesQuery = (
  accountBookId: number | string,
  filter?: ExpenseSearchFilter,
  options?: Omit<UseQueryOptions<GetExpensesResponse>, 'queryKey' | 'queryFn'>,
) => {
  return useQuery({
    queryKey: ['expenses', accountBookId, filter],
    queryFn: () => getExpenses(accountBookId, filter),
    ...options,
  });
};

/** 지출 내역 목록 조회 Suspense Query */
export const useGetExpensesSuspenseQuery = (
  accountBookId: number | string,
  filter?: ExpenseSearchFilter,
) => {
  return useSuspenseQuery({
    queryKey: ['expenses', accountBookId, filter],
    queryFn: () => getExpenses(accountBookId, filter),
  });
};

/** 지출 상세 조회 Query */
export const useGetExpenseDetailQuery = (
  accountBookId: number | string,
  expenseId: number | string,
  options?: Omit<UseQueryOptions<ExpenseResponse>, 'queryKey' | 'queryFn'>,
) => {
  return useQuery({
    queryKey: ['expense', accountBookId, expenseId],
    queryFn: () => getExpenseDetail(accountBookId, expenseId),
    ...options,
  });
};

/** 지출 상세 조회 Suspense Query */
export const useGetExpenseDetailSuspenseQuery = (
  accountBookId: number | string,
  expenseId: number | string,
) => {
  return useSuspenseQuery({
    queryKey: ['expense', accountBookId, expenseId],
    queryFn: () => getExpenseDetail(accountBookId, expenseId),
  });
};

/** 거래처명 검색 Query */
export const useSearchMerchantNamesQuery = (
  accountBookId: number | string,
  query: string,
  limit?: number,
  options?: Omit<
    UseQueryOptions<MerchantNamesResponse>,
    'queryKey' | 'queryFn'
  >,
) => {
  return useQuery({
    queryKey: ['merchantNames', accountBookId, query, limit],
    queryFn: () => searchMerchantNames(accountBookId, query, limit),
    enabled: query.length > 0, // 검색어가 있을 때만 실행
    ...options,
  });
};

/** 지출 생성 Mutation */
export const useCreateExpenseMutation = (
  options?: UseMutationOptions<
    ExpenseResponse,
    Error,
    { accountBookId: number | string; data: CreateExpenseRequest }
  >,
) => {
  return useMutation({
    mutationFn: ({ accountBookId, data }) => createExpense(accountBookId, data),
    ...options,
  });
};

/** 지출 수정 Mutation */
export const useUpdateExpenseMutation = (
  options?: UseMutationOptions<
    ExpenseResponse,
    Error,
    {
      accountBookId: number | string;
      expenseId: number | string;
      data: CreateExpenseRequest;
    }
  >,
) => {
  return useMutation({
    mutationFn: ({ accountBookId, expenseId, data }) =>
      updateExpense(accountBookId, expenseId, data),
    ...options,
  });
};

/** 지출 삭제 Mutation */
export const useDeleteExpenseMutation = (
  options?: UseMutationOptions<
    void,
    Error,
    { accountBookId: number | string; expenseId: number | string }
  >,
) => {
  return useMutation({
    mutationFn: ({ accountBookId, expenseId }) =>
      deleteExpense(accountBookId, expenseId),
    ...options,
  });
};
