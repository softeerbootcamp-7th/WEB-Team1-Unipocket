import {
  queryOptions,
  useMutation,
  useQuery,
  useSuspenseQuery,
} from '@tanstack/react-query';
import { toast } from 'sonner';

import {
  createCard,
  deleteCard,
  deleteUser,
  getCardCompanies,
  getCards,
  getUser,
} from '@/api/users/api';
import type { CreateCardRequest } from '@/api/users/type';
import { queryClient } from '@/main';

// --- Query Options ---

const userQueryOptions = queryOptions({
  // 주의: auth/query.ts의 쿼리 키와 반드시 똑같이 맞춰야 합니다!
  // 기존에 ['getUser'] 였다면 그것으로 통일하거나, 아래처럼 ['user', 'me']로 둘 다 변경해 주세요.
  queryKey: ['user', 'me'],
  queryFn: getUser,
  staleTime: 1000 * 60 * 5,
});

const cardsQueryOptions = queryOptions({
  queryKey: ['users', 'cards', 'list'],
  queryFn: getCards,
});

const cardCompaniesQueryOptions = queryOptions({
  queryKey: ['users', 'cards', 'companies'],
  queryFn: getCardCompanies,
  staleTime: Infinity,
});

// --- Queries ---

const useGetUserQuery = () => {
  return useSuspenseQuery(userQueryOptions);
};

const useGetCardsQuery = () => {
  return useSuspenseQuery(cardsQueryOptions);
};

const useGetCardCompaniesQuery = () => {
  return useQuery(cardCompaniesQueryOptions);
};

// --- Mutations ---

const useDeleteUserMutation = () =>
  useMutation({
    mutationFn: () => deleteUser(),
    onSuccess: () => {
      queryClient.clear(); // 회원 탈퇴 시 모든 캐시 초기화
      toast.success('회원 탈퇴가 완료되었어요.');
    },
    onError: () => {
      toast.error('회원 탈퇴에 실패했어요.');
    },
  });

const useCreateCardMutation = () =>
  useMutation({
    mutationFn: (data: CreateCardRequest) => createCard(data),
    onSuccess: () => {
      // ✅ 하드코딩 제거: cardsQueryOptions에서 정의한 키를 바로 가져와 사용합니다.
      queryClient.invalidateQueries({ queryKey: cardsQueryOptions.queryKey });
      toast.success('카드가 등록되었어요.');
    },
    onError: () => {
      toast.error('카드 등록에 실패했어요.');
    },
  });

const useDeleteCardMutation = () =>
  useMutation({
    mutationFn: (cardId: number) => deleteCard(cardId),
    onSuccess: () => {
      // ✅ 하드코딩 제거
      queryClient.invalidateQueries({ queryKey: cardsQueryOptions.queryKey });
      toast.success('카드가 삭제되었어요.');
    },
    onError: () => {
      toast.error('카드 삭제에 실패했어요.');
    },
  });

export {
  cardCompaniesQueryOptions,
  cardsQueryOptions,
  useCreateCardMutation,
  useDeleteCardMutation,
  useDeleteUserMutation,
  useGetCardCompaniesQuery,
  useGetCardsQuery,
  useGetUserQuery,
  userQueryOptions,
};
