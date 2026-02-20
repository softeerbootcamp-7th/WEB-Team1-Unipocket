import {
  queryOptions,
  useMutation,
  useQuery,
  useSuspenseQuery,
} from '@tanstack/react-query';
import { toast } from 'sonner';

import type { CurrencyType } from '@/types/currency';

import {
  createAccountBook,
  deleteAccountBook,
  getAccountBookDetail,
  getAccountBooks,
  getAnalysis,
  updateAccountBook,
  updateAccountBookBudget,
} from '@/api/account-books/api';
import type {
  CreateAccountBookRequest,
  GetAnalysisResponse,
  UpdateAccountBookRequest,
} from '@/api/account-books/type';
import { queryClient } from '@/main';
import { useAccountBookStore } from '@/stores/useAccountBookStore';

const accountBooksQueryOptions = queryOptions({
  queryKey: ['accountBooks', 'list'],
  queryFn: getAccountBooks,
  staleTime: 1000 * 30,
});

const useGetAccountBooksQuery = () => {
  return useSuspenseQuery(accountBooksQueryOptions);
};

const useCreateAccountBookMutation = () =>
  useMutation({
    mutationFn: (data: CreateAccountBookRequest) => createAccountBook(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['accountBooks', 'list'] });
      toast.success('가계부가 생성되었어요.');
    },
    onError: () => {
      toast.error('가계부 생성에 실패했어요.');
    },
  });

const accountBookDetailQueryOptions = (accountBookId: number | null) =>
  queryOptions({
    queryKey: ['accountBooks', 'detail', accountBookId],
    queryFn: () => getAccountBookDetail(accountBookId as number),
    enabled: !!accountBookId,
  });

const useAccountBookDetailQuery = (accountBookId: number | null) =>
  useQuery(accountBookDetailQueryOptions(accountBookId));

const useDeleteAccountBookMutation = () =>
  useMutation({
    mutationFn: (accountBookId: number) => deleteAccountBook(accountBookId),
    onSuccess: (_, accountBookId) => {
      queryClient.invalidateQueries({ queryKey: ['accountBooks', 'list'] });
      queryClient.removeQueries({
        queryKey: ['accountBooks', 'detail', accountBookId],
      });
      toast.success('가계부가 삭제되었어요.');
    },
    onError: () => {
      toast.error('가계부 삭제에 실패했어요.');
    },
  });

const useUpdateAccountBookMutation = () =>
  useMutation({
    mutationFn: ({
      accountBookId,
      data,
    }: {
      accountBookId: number;
      data: UpdateAccountBookRequest;
    }) => updateAccountBook(accountBookId, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['accountBooks', 'list'] });
      queryClient.invalidateQueries({
        queryKey: ['accountBooks', 'detail', variables.accountBookId],
      });
      toast.success('가계부가 수정되었어요.');
    },
    onError: () => {
      toast.error('가계부 수정에 실패했어요.');
    },
  });

const useAnalysisQuery = (
  accountBookId: number | null,
  year: number,
  month: number,
  currencyType: CurrencyType,
) =>
  useQuery<GetAnalysisResponse>({
    queryKey: ['analysis', accountBookId, year, month, currencyType],
    queryFn: () =>
      getAnalysis(accountBookId as number, year, month, currencyType),
    enabled: !!accountBookId,
    placeholderData: (previousData) => previousData,
  });

export {
  accountBookDetailQueryOptions,
  accountBooksQueryOptions,
  useAccountBookDetailQuery,
  useAnalysisQuery,
  useCreateAccountBookMutation,
  useDeleteAccountBookMutation,
  useGetAccountBooksQuery,
  useUpdateAccountBookMutation,
};

export const useUpdateAccountBookBudgetMutation = () => {
  const accountBookId = useAccountBookStore((state) => state.accountBook?.id);

  return useMutation({
    // mutationFn은 budget 값 하나만 인자로 받습니다.
    mutationFn: (budget: number) => {
      // id가 없는 방어 코드 추가 (옵셔널 체이닝 및 타입 단언(!)의 불안정성 해소)
      if (!accountBookId) throw new Error('Account Book ID is missing');
      return updateAccountBookBudget(accountBookId, budget);
    },
    onSuccess: () => {
      // widgetType이 'BUDGET'인 모든 쿼리를 무효화
      // 배열의 앞에서부터 매칭되므로 하위 옵션(currencyType 등)과 무관하게 모두 무효화됨
      queryClient.invalidateQueries({
        queryKey: ['widget', accountBookId, 'BUDGET'],
      });
      toast.success('예산이 저장되었어요.');
    },
    onError: () => {
      toast.error('예산 저장에 실패했어요.');
    },
  });
};
