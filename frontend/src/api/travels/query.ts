import { useMutation, useQuery } from '@tanstack/react-query';
import { toast } from 'sonner';

import {
  createTravel,
  deleteTravel,
  getTravelAmount,
  getTravelDetail,
  getTravelImageUrl,
  getTravelPresignedUrl,
  getTravels,
  patchTravel,
  updateTravel,
} from '@/api/travels/api';
import type {
  CreateTravelRequest,
  GetPresignedUrlRequest,
  PatchTravelRequest,
  UpdateTravelRequest,
} from '@/api/travels/type';
import { queryClient } from '@/main';
import { useRequiredAccountBook } from '@/stores/accountBookStore';

export const travelKeys = {
  all: ['travels'] as const,

  list: (accountBookId: number | string | undefined) =>
    [...travelKeys.all, 'list', accountBookId] as const,

  detail: (
    accountBookId: number | string | undefined,
    travelId: number | string | undefined,
  ) => [...travelKeys.all, 'detail', accountBookId, travelId] as const,

  amount: (
    accountBookId: number | string | undefined,
    travelId: number | string | undefined,
  ) => [...travelKeys.all, 'amount', accountBookId, travelId] as const,

  imageUrl: (accountBookId: number | string | undefined) =>
    [...travelKeys.all, 'imageUrl', accountBookId] as const,
};

export const useGetTravelsQuery = () => {
  const { accountBookId } = useRequiredAccountBook();

  return useQuery({
    queryKey: travelKeys.list(accountBookId),
    queryFn: () => getTravels(accountBookId),
    enabled: !!accountBookId,
  });
};

export const useGetTravelDetailQuery = (travelId: number | string) => {
  const { accountBookId } = useRequiredAccountBook();

  return useQuery({
    queryKey: travelKeys.detail(accountBookId, travelId),
    queryFn: () => getTravelDetail(accountBookId, travelId),
    enabled: !!accountBookId && !!travelId,
  });
};

export const useGetTravelAmountQuery = (travelId: number | string) => {
  const { accountBookId } = useRequiredAccountBook();

  return useQuery({
    queryKey: travelKeys.amount(accountBookId, travelId),
    queryFn: () => getTravelAmount(accountBookId, travelId),
    enabled: !!accountBookId && !!travelId,
  });
};

export const useGetTravelImageUrlQuery = () => {
  const { accountBookId } = useRequiredAccountBook();

  return useQuery({
    queryKey: travelKeys.imageUrl(accountBookId),
    queryFn: () => getTravelImageUrl(accountBookId),
    enabled: !!accountBookId,
  });
};

export const useCreateTravelMutation = () => {
  const { accountBookId } = useRequiredAccountBook();

  return useMutation({
    mutationFn: (data: CreateTravelRequest) =>
      createTravel(accountBookId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: travelKeys.list(accountBookId),
      });
      toast.success('여행이 생성되었어요.');
    },
    onError: () => {
      toast.error('여행 생성에 실패했어요.');
    },
  });
};

export const useUpdateTravelMutation = (travelId: number | string) => {
  const { accountBookId } = useRequiredAccountBook();

  return useMutation({
    mutationFn: (data: UpdateTravelRequest) =>
      updateTravel(accountBookId, travelId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: travelKeys.list(accountBookId),
      });
      queryClient.invalidateQueries({
        queryKey: travelKeys.detail(accountBookId, travelId),
      });
      toast.success('여행이 수정되었어요.');
    },
    onError: () => {
      toast.error('여행 수정에 실패했어요.');
    },
  });
};

export const usePatchTravelMutation = (travelId: number | string) => {
  const { accountBookId } = useRequiredAccountBook();

  return useMutation({
    mutationFn: (data: PatchTravelRequest) =>
      patchTravel(accountBookId, travelId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: travelKeys.list(accountBookId),
      });
      queryClient.invalidateQueries({
        queryKey: travelKeys.detail(accountBookId, travelId),
      });
      toast.success('여행이 수정되었어요.');
    },
    onError: () => {
      toast.error('여행 수정에 실패했어요.');
    },
  });
};

export const useDeleteTravelMutation = () => {
  const { accountBookId } = useRequiredAccountBook();

  return useMutation({
    mutationFn: (travelId: number | string) =>
      deleteTravel(accountBookId, travelId),
    onSuccess: (_, travelId) => {
      queryClient.invalidateQueries({
        queryKey: travelKeys.list(accountBookId),
      });
      queryClient.removeQueries({
        queryKey: travelKeys.detail(accountBookId, travelId),
      });
      toast.success('여행이 삭제되었어요.');
    },
    onError: () => {
      toast.error('여행 삭제에 실패했어요.');
    },
  });
};

export const useGetTravelPresignedUrlMutation = () => {
  const { accountBookId } = useRequiredAccountBook();

  return useMutation({
    mutationFn: (data: GetPresignedUrlRequest) =>
      getTravelPresignedUrl(accountBookId, data),
    onError: () => {
      toast.error('이미지 업로드 URL 발급에 실패했어요.');
    },
  });
};
