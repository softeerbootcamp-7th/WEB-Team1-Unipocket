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

const userKeys = {
  all: ['users'] as const,
  me: () => [...userKeys.all, 'me'] as const,
  cards: () => [...userKeys.all, 'cards'] as const,
  cardList: () => [...userKeys.cards(), 'list'] as const,
  cardCompanies: () => [...userKeys.cards(), 'companies'] as const,
};

// ==========================================
// Query Options
// ==========================================

const userQueryOptions = queryOptions({
  queryKey: userKeys.me(),
  queryFn: getUser,
  staleTime: 1000 * 60 * 5, // 5분
});

const cardsQueryOptions = queryOptions({
  queryKey: userKeys.cardList(),
  queryFn: getCards,
});

const cardCompaniesQueryOptions = queryOptions({
  queryKey: userKeys.cardCompanies(),
  queryFn: getCardCompanies,
});

// ==========================================
// Queries
// ==========================================

const useGetUserQuery = () => {
  return useSuspenseQuery(userQueryOptions);
};

const useGetCardsQuery = () => {
  return useSuspenseQuery(cardsQueryOptions);
};

const useGetCardCompaniesQuery = () => {
  return useQuery(cardCompaniesQueryOptions);
};

// ==========================================
// Mutations
// ==========================================

const useDeleteUserMutation = () =>
  useMutation({
    mutationFn: () => deleteUser(),
    onSuccess: () => {
      queryClient.clear();
      toast.success('회원 탈퇴가 완료되었어요.');
      window.location.href = '/';
    },
    onError: () => {
      toast.error('회원 탈퇴에 실패했어요.');
    },
  });

const useCreateCardMutation = () =>
  useMutation({
    mutationFn: (data: CreateCardRequest) => createCard(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: userKeys.cardList() });
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
      queryClient.invalidateQueries({ queryKey: userKeys.cardList() });
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
  userKeys,
  userQueryOptions,
};
