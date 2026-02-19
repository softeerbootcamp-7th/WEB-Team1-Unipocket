import { useMutation } from '@tanstack/react-query';

import { getPresignedUrl, startParse } from '@/api/temporary-expenses/api';
import type {
  ParseRequest,
  ParseResponse,
  PresignedUrlRequest,
  PresignedUrlResponse,
} from '@/api/temporary-expenses/type';

export const temporaryExpenseKeys = {
  all: ['temporaryExpenses'] as const,
};

export const usePresignedUrlMutation = (accountBookId: number) =>
  useMutation<PresignedUrlResponse, Error, PresignedUrlRequest>({
    mutationFn: (data) => getPresignedUrl(accountBookId, data),
  });

export const useStartParseMutation = (accountBookId: number) =>
  useMutation<ParseResponse, Error, ParseRequest>({
    mutationFn: (data) => startParse(accountBookId, data),
  });
