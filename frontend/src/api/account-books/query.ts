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
import { useRequiredAccountBook } from '@/stores/accountBookStore';

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
  const accountBookId = useRequiredAccountBook().id;

  return useMutation({
    mutationFn: (budget: number) => {
      return updateAccountBookBudget(accountBookId, budget);
    },
    onSuccess: () => {
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
