import { useMutation } from '@tanstack/react-query';

import { getPresignedUrl, startParse } from '@/api/temporary-expenses/api';
import type {
  GetPresignedUrlRequest,
  GetPresignedUrlResponse,
  StartParseRequest,
  StartParseResponse,
} from '@/api/temporary-expenses/type';

export const temporaryExpenseKeys = {
  all: ['temporaryExpenses'] as const,
};

export const usePresignedUrlMutation = (accountBookId: number) =>
  useMutation<GetPresignedUrlResponse, Error, GetPresignedUrlRequest>({
    mutationFn: (data) => getPresignedUrl(accountBookId, data),
  });

export const useStartParseMutation = (accountBookId: number) =>
  useMutation<StartParseResponse, Error, StartParseRequest>({
    mutationFn: (data) => startParse(accountBookId, data),
  });
