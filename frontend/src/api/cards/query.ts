import { useMutation, useSuspenseQuery } from '@tanstack/react-query';
import { toast } from 'sonner';

import {
  createCard,
  deleteCard,
  getCardCompanies,
  getCards,
  updateCardNickname,
} from '@/api/cards/api';
import { queryClient } from '@/main';

import type { CreateCardRequest, UpdateCardNicknameRequest } from './type';

export const useCardCompaniesSuspenseQuery = () =>
  useSuspenseQuery<Awaited<ReturnType<typeof getCardCompanies>>>({
    queryKey: ['cards', 'companies'],
    queryFn: getCardCompanies,
    staleTime: Infinity,
  });

export const useCardsSuspenseQuery = () =>
  useSuspenseQuery<Awaited<ReturnType<typeof getCards>>>({
    queryKey: ['cards'],
    queryFn: getCards,
    staleTime: 1000 * 30,
  });

export const useCreateCardMutation = () =>
  useMutation({
    mutationFn: (data: CreateCardRequest) => createCard(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cards'] });
      toast.success('카드가 등록되었어요.');
    },
    onError: () => {
      toast.error('카드 등록에 실패했어요.');
    },
  });

export const useDeleteCardMutation = () =>
  useMutation({
    mutationFn: (cardId: number) => deleteCard(cardId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cards'] });
      toast.success('카드가 삭제되었어요.');
    },
    onError: () => {
      toast.error('카드 삭제에 실패했어요.');
    },
  });

export const useUpdateCardNicknameMutation = () =>
  useMutation({
    mutationFn: ({
      cardId,
      data,
    }: {
      cardId: number;
      data: UpdateCardNicknameRequest;
    }) => updateCardNickname(cardId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cards'] });
      toast.success('카드 별명이 수정되었어요.');
    },
    onError: () => {
      toast.error('카드 별명 수정에 실패했어요.');
    },
  });
