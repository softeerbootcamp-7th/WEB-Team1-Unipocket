import {
  queryOptions,
  useMutation,
  useQuery,
  useSuspenseQuery,
} from '@tanstack/react-query';
import { toast } from 'sonner';

import { accountBookKeys } from '@/api/account-books/query';
import {
  bulkUpdateExpenses,
  createManualExpense,
  deleteExpense,
  getExpenseDetail,
  getExpenseFileUrl,
  getExpenses,
  searchMerchantNames,
  updateExpense,
} from '@/api/expenses/api';
import type {
  BulkUpdateExpenseRequest,
  CreateManualExpenseRequest,
  ExpenseSearchFilter,
  UpdateExpenseRequest,
} from '@/api/expenses/type';
import { travelKeys } from '@/api/travels/query';
import { widgetKeys } from '@/api/widget/query';
import { queryClient } from '@/main';

export const expenseKeys = {
  all: ['expenses'] as const,
  lists: (accountBookId: number | string) =>
    [...expenseKeys.all, 'list', accountBookId] as const,
  list: (accountBookId: number | string, filter?: ExpenseSearchFilter) =>
    [...expenseKeys.lists(accountBookId), filter] as const,
  details: (accountBookId: number | string) =>
    [...expenseKeys.all, 'detail', accountBookId] as const,
  detail: (accountBookId: number | string, expenseId: number | string) =>
    [...expenseKeys.details(accountBookId), expenseId] as const,
  fileUrls: (accountBookId: number | string) =>
    [...expenseKeys.all, 'fileUrl', accountBookId] as const,
  fileUrl: (accountBookId: number | string, expenseId: number | string) =>
    [...expenseKeys.fileUrls(accountBookId), expenseId] as const,
  merchants: (accountBookId: number | string, query: string, limit?: number) =>
    [...expenseKeys.all, 'merchants', accountBookId, query, limit] as const,
};

const invalidateRelatedQueries = (
  accountBookId: number | string,
  travelId?: number | string | null,
) => {
  queryClient.invalidateQueries({ queryKey: expenseKeys.lists(accountBookId) });
  queryClient.invalidateQueries({
    queryKey: widgetKeys.allDetails(accountBookId),
  });
  queryClient.invalidateQueries({
    queryKey: travelKeys.allTravelWidgets(accountBookId),
  });
  queryClient.invalidateQueries({
    queryKey: accountBookKeys.amount(accountBookId),
  });
  queryClient.invalidateQueries({
    queryKey: travelKeys.allAmounts(accountBookId),
  });
  if (travelId) {
    queryClient.invalidateQueries({
      queryKey: travelKeys.amount(accountBookId, travelId),
    });
  }
};

/** 지출 상세 Query Options */
const expenseDetailQueryOptions = (
  accountBookId: number | string,
  expenseId: number | string,
) =>
  queryOptions({
    queryKey: expenseKeys.detail(accountBookId, expenseId),
    queryFn: () => getExpenseDetail(accountBookId, expenseId),
  });

/** 지출 상세 조회 (일반) */
export const useGetExpenseDetailQuery = (
  accountBookId: number | string,
  expenseId: number | string,
) => useQuery(expenseDetailQueryOptions(accountBookId, expenseId));

/** 지출 수정 Mutation */
export const useUpdateExpenseMutation = () =>
  useMutation({
    mutationFn: ({
      accountBookId,
      expenseId,
      data,
    }: {
      accountBookId: number | string;
      expenseId: number | string;
      data: UpdateExpenseRequest;
    }) => updateExpense(accountBookId, expenseId, data),
    onSuccess: (_, variables) => {
      invalidateRelatedQueries(
        variables.accountBookId,
        variables.data.travelId,
      );
      queryClient.invalidateQueries({
        queryKey: expenseKeys.detail(
          variables.accountBookId,
          variables.expenseId,
        ),
      });
      toast.success('지출 내역이 수정되었어요.');
    },
    onError: () => {
      toast.error('지출 수정에 실패했어요.');
    },
  });

/** 지출 일괄 수정 Mutation */
export const useBulkUpdateExpensesMutation = () =>
  useMutation({
    mutationFn: ({
      accountBookId,
      data,
    }: {
      accountBookId: number | string;
      data: BulkUpdateExpenseRequest;
      travelId?: number | string;
    }) => bulkUpdateExpenses(accountBookId, data),
    onSuccess: (_, variables) => {
      invalidateRelatedQueries(variables.accountBookId, variables.travelId);
      toast.success('지출 내역이 일괄 수정되었어요.');
    },
    onError: () => {
      toast.error('지출 일괄 수정에 실패했어요.');
    },
  });

/** 지출 삭제 Mutation */
export const useDeleteExpenseMutation = () =>
  useMutation({
    mutationFn: ({
      accountBookId,
      expenseId,
    }: {
      accountBookId: number | string;
      expenseId: number | string;
      travelId?: number | string;
    }) => deleteExpense(accountBookId, expenseId),
    onSuccess: (_, variables) => {
      invalidateRelatedQueries(variables.accountBookId, variables.travelId);
      queryClient.removeQueries({
        queryKey: expenseKeys.detail(
          variables.accountBookId,
          variables.expenseId,
        ),
      });
      toast.success('지출 내역이 삭제되었어요.');
    },
    onError: () => {
      toast.error('지출 삭제에 실패했어요.');
    },
  });

/** 지출 생성 Mutation */
export const useCreateManualExpenseMutation = () =>
  useMutation({
    mutationFn: ({
      accountBookId,
      data,
    }: {
      accountBookId: number | string;
      data: CreateManualExpenseRequest;
    }) => createManualExpense(accountBookId, data),
    onSuccess: (_, variables) => {
      invalidateRelatedQueries(
        variables.accountBookId,
        variables.data.travelId,
      );
      toast.success('지출 내역이 생성되었어요.');
    },
    onError: () => {
      toast.error('지출 생성에 실패했어요.');
    },
  });

/** 지출 목록 Query Options */
const expensesQueryOptions = (
  accountBookId: number | string,
  filter?: ExpenseSearchFilter,
) =>
  queryOptions({
    queryKey: expenseKeys.list(accountBookId, filter),
    queryFn: () => getExpenses(accountBookId, filter),
  });

/** 지출 내역 목록 조회 (Suspense) */
export const useGetExpensesQuery = (
  accountBookId: number | string,
  filter?: ExpenseSearchFilter,
) => useSuspenseQuery(expensesQueryOptions(accountBookId, filter));

/** 지출 파일 URL 조회 Query */
export const useGetExpenseFileUrlQuery = (
  accountBookId: number | string,
  expenseId: number | string,
) => useQuery(expenseFileUrlQueryOptions(accountBookId, expenseId));

/** 지출 파일 URL Query Options */
const expenseFileUrlQueryOptions = (
  accountBookId: number | string,
  expenseId: number | string,
) =>
  queryOptions({
    queryKey: expenseKeys.fileUrl(accountBookId, expenseId),
    queryFn: () => getExpenseFileUrl(accountBookId, expenseId),
  });

/** 거래처명 검색 Query */
export const useSearchMerchantNamesQuery = (
  accountBookId: number | string,
  query: string,
  limit?: number,
) =>
  useQuery({
    queryKey: expenseKeys.merchants(accountBookId, query, limit),
    queryFn: () => searchMerchantNames(accountBookId, query, limit),
    enabled: query.length > 0,
  });
