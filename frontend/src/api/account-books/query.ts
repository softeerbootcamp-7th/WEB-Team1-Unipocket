import { useMutation, useQuery } from '@tanstack/react-query';
import { toast } from 'sonner';

import {
  createAccountBook,
  deleteAccountBook,
  getAccountBookDetail,
  getAccountBooks,
  setMainAccountBook,
  updateAccountBook,
} from '@/api/account-books/api';
import type {
  AccountBookDetail,
  AccountBookSummary,
  CreateAccountBookRequest,
  UpdateAccountBookRequest,
} from '@/api/account-books/type';
import { queryClient } from '@/main';

export const useAccountBooksQuery = () =>
  useQuery<AccountBookSummary[]>({
    queryKey: ['accountBooks'],
    queryFn: getAccountBooks,
    staleTime: 1000 * 30,
  });

export const useAccountBookDetailQuery = (accountBookId: number | null) =>
  useQuery<AccountBookDetail>({
    queryKey: ['accountBook', accountBookId],
    queryFn: () => getAccountBookDetail(accountBookId as number),
    enabled: !!accountBookId,
  });

export const useCreateAccountBookMutation = () =>
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

export const useUpdateAccountBookMutation = () =>
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

export const useDeleteAccountBookMutation = () =>
  useMutation({
    mutationFn: (accountBookId: number) => deleteAccountBook(accountBookId),
    onSuccess: (_, accountBookId) => {
      queryClient.setQueryData<AccountBookSummary[]>(
        ['accountBooks'],
        (prev) => {
          if (!prev) return prev;
          const deleted = prev.find((book) => book.id === accountBookId);
          const remaining = prev.filter((book) => book.id !== accountBookId);
          if (deleted?.isMain && remaining.length > 0) {
            remaining[0] = { ...remaining[0], isMain: true };
          }
          return remaining;
        },
      );
      queryClient.invalidateQueries({ queryKey: ['accountBooks'] });
      toast.success('가계부가 삭제되었어요.');
    },
    onError: () => {
      toast.error('가계부 삭제에 실패했어요.');
    },
  });

export const useSetMainAccountBookMutation = () =>
  useMutation({
    mutationFn: (accountBookId: number) => setMainAccountBook(accountBookId),
    onMutate: async (accountBookId) => {
      await queryClient.cancelQueries({ queryKey: ['accountBooks'] });
      const previous = queryClient.getQueryData<AccountBookSummary[]>([
        'accountBooks',
      ]);
      queryClient.setQueryData<AccountBookSummary[]>(
        ['accountBooks'],
        (prev) =>
          prev?.map((book) => ({
            ...book,
            isMain: book.id === accountBookId,
          })) ?? prev,
      );
      return { previous };
    },
    onError: (_error, _accountBookId, context) => {
      if (context?.previous) {
        queryClient.setQueryData(['accountBooks'], context.previous);
      }
      toast.error('메인 가계부 변경에 실패했어요.');
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['accountBooks'] });
      toast.success('메인 가계부가 변경되었어요.');
    },
  });

export type { AccountBookDetail };
