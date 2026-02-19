import {
  queryOptions,
  useMutation,
  useQuery,
  useSuspenseQuery,
} from '@tanstack/react-query';
import { toast } from 'sonner';

import {
  createAccountBook,
  deleteAccountBook,
  getAccountBookDetail,
  getAccountBooks,
  updateAccountBook,
} from '@/api/account-books/api';
import type {
  CreateAccountBookRequest,
  UpdateAccountBookRequest,
} from '@/api/account-books/type';
import { queryClient } from '@/main';

const accountBooksQueryOptions = queryOptions({
  queryKey: ['accountBooks'],
  queryFn: getAccountBooks,
});

const useGetAccountBooksQuery = () => {
  return useSuspenseQuery(accountBooksQueryOptions);
};

//////
const useAccountBooksQuery = () =>
  useQuery({
    queryKey: ['accountBooks'],
    queryFn: getAccountBooks,
    staleTime: 1000 * 30,
  });

const useCreateAccountBookMutation = () =>
  useMutation({
    mutationFn: (data: CreateAccountBookRequest) => createAccountBook(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['accountBooks'] });
      toast.success('가계부가 생성되었어요.');
    },
    onError: () => {
      toast.error('가계부 생성에 실패했어요.');
    },
  });

const accountBookDetailQueryOptions = (accountBookId: number | null) =>
  queryOptions({
    queryKey: ['accountBookDetail', accountBookId],
    queryFn: () => getAccountBookDetail(accountBookId as number),
    enabled: !!accountBookId,
  });

const useAccountBookDetailQuery = (accountBookId: number | null) =>
  useQuery(accountBookDetailQueryOptions(accountBookId));

const useDeleteAccountBookMutation = () =>
  useMutation({
    mutationFn: (accountBookId: number) => deleteAccountBook(accountBookId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['accountBooks'] });
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
      queryClient.invalidateQueries({ queryKey: ['accountBooks'] });
      queryClient.invalidateQueries({
        queryKey: ['accountBook', variables.accountBookId],
      });
      toast.success('가계부가 수정되었어요.');
    },
    onError: () => {
      toast.error('가계부 수정에 실패했어요.');
    },
  });

export {
  accountBookDetailQueryOptions,
  accountBooksQueryOptions,
  useAccountBookDetailQuery,
  useAccountBooksQuery,
  useCreateAccountBookMutation,
  useDeleteAccountBookMutation,
  useGetAccountBooksQuery,
  useUpdateAccountBookMutation,
};
