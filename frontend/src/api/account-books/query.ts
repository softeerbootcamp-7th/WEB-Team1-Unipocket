import {
  queryOptions,
  useMutation,
  useQuery,
  useSuspenseQuery,
} from '@tanstack/react-query';
import { useNavigate } from '@tanstack/react-router';
import { toast } from 'sonner';

import type { CurrencyType } from '@/types/currency';

import {
  createAccountBook,
  deleteAccountBook,
  getAccountBookAmount,
  getAccountBookDetail,
  getAccountBooks,
  getAnalysis,
  getExchangeRate,
  updateAccountBook,
  updateAccountBookBudget,
} from '@/api/account-books/api';
import type {
  CreateAccountBookRequest,
  GetAnalysisResponse,
  UpdateAccountBookRequest,
} from '@/api/account-books/type';
import { widgetKeys } from '@/api/widget/query';
import type { CurrencyCode } from '@/data/country/currencyCode';
import { queryClient } from '@/main';
import {
  useAccountBookStore,
  useRequiredAccountBook,
} from '@/stores/accountBookStore';

const accountBooksQueryOptions = queryOptions({
  queryKey: ['accountBooks', 'list'],
  queryFn: getAccountBooks,
  staleTime: 1000 * 30,
  meta: {
    errorMessage: '가계부 목록을 불러오지 못했어요.',
  },
});

const useGetAccountBooksQuery = () => {
  return useSuspenseQuery(accountBooksQueryOptions);
};

const useCreateAccountBookMutation = () => {
  const setAccountBook = useAccountBookStore((s) => s.setAccountBook);

  return useMutation({
    mutationFn: (data: CreateAccountBookRequest) => createAccountBook(data),
    onSuccess: (createdAccountBook) => {
      setAccountBook(createdAccountBook);
      queryClient.invalidateQueries({ queryKey: ['accountBooks', 'list'] });
      toast.success('가계부가 생성되었어요.');
    },
    onError: () => {
      toast.error('가계부 생성에 실패했어요.');
    },
  });
};

const accountBookDetailQueryOptions = (accountBookId: number | null) =>
  queryOptions({
    queryKey: ['accountBooks', 'detail', accountBookId],
    queryFn: () => getAccountBookDetail(accountBookId as number),
    enabled: !!accountBookId,
    meta: {
      errorMessage: '가계부 상세 정보를 불러오지 못했어요.',
    },
  });

const useAccountBookDetailQuery = (accountBookId: number | null) =>
  useQuery(accountBookDetailQueryOptions(accountBookId));

const useDeleteAccountBookMutation = () => {
  const navigate = useNavigate();
  const clearAccountBook = useAccountBookStore((s) => s.clearAccountBook);

  return useMutation({
    mutationFn: (accountBookId: number) => deleteAccountBook(accountBookId),
    onSuccess: (_, accountBookId) => {
      const cached = queryClient.getQueryData<{ accountBookId: number }[]>([
        'accountBooks',
        'list',
      ]);

      const isLastOne = !cached || cached.length <= 1;

      if (isLastOne) {
        // removeQueries를 navigate 완료 후에 호출
        // → 컴포넌트가 언마운트된 뒤 구독자가 없어 리페치(404) 방지
        void navigate({ to: '/init' }).then(() => {
          queryClient.removeQueries({
            queryKey: ['accountBooks', 'detail', accountBookId],
          });
          clearAccountBook();
        });
      } else {
        queryClient.removeQueries({
          queryKey: ['accountBooks', 'detail', accountBookId],
        });
        queryClient.invalidateQueries({ queryKey: ['accountBooks', 'list'] });
        toast.success('가계부가 삭제되었어요.');
      }
    },
    onError: () => {
      toast.error('가계부 삭제에 실패했어요.');
    },
  });
};

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

const useUpdateAccountBookBudgetMutation = () => {
  const accountBookId = useRequiredAccountBook().accountBookId;

  return useMutation({
    mutationFn: (budget: number) => {
      return updateAccountBookBudget(accountBookId, budget);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: widgetKeys.detailType(accountBookId, 'BUDGET'),
      });
      toast.success('예산이 저장되었어요.');
    },
    onError: () => {
      toast.error('예산 저장에 실패했어요.');
    },
  });
};

const useGetExchangeRateQuery = (
  occurredAt: string,
  baseCurrencyCode: CurrencyCode,
  localCurrencyCode: CurrencyCode,
) => {
  const todayStr = new Date().toISOString().slice(0, 10);
  const isToday = occurredAt.startsWith(todayStr);
  return useQuery({
    queryKey: ['exchangeRate', occurredAt, baseCurrencyCode, localCurrencyCode],
    queryFn: () =>
      getExchangeRate(occurredAt, baseCurrencyCode, localCurrencyCode),
    enabled: !!occurredAt && !!baseCurrencyCode && !!localCurrencyCode,
    staleTime: isToday ? 1000 * 60 * 30 : Infinity,
    placeholderData: (previousData) => previousData,
  });
};

const useGetAccountBookAmountQuery = () => {
  const { accountBookId } = useRequiredAccountBook();

  return useQuery({
    queryKey: ['accountBooks', 'amount', accountBookId],
    queryFn: () => getAccountBookAmount(accountBookId),
    enabled: !!accountBookId,
  });
};

export {
  accountBookDetailQueryOptions,
  accountBooksQueryOptions,
  useAccountBookDetailQuery,
  useAnalysisQuery,
  useCreateAccountBookMutation,
  useDeleteAccountBookMutation,
  useGetAccountBookAmountQuery,
  useGetAccountBooksQuery,
  useGetExchangeRateQuery,
  useUpdateAccountBookBudgetMutation,
  useUpdateAccountBookMutation,
};
