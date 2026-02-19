import { customFetch } from '@/api/config/client';
import { ENDPOINTS } from '@/api/config/endpoint';
import type {
  ParseRequest,
  ParseResponse,
  PresignedUrlRequest,
  PresignedUrlResponse,
} from '@/api/temporary-expenses/type';

export const getPresignedUrl = (
  accountBookId: number,
  data: PresignedUrlRequest,
): Promise<PresignedUrlResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.TEMPORARY_EXPENSES.PRESIGNED_URL(accountBookId),
    options: {
      method: 'POST',
      body: JSON.stringify(data),
    },
  });
};

export const startParse = (
  accountBookId: number,
  data: ParseRequest,
): Promise<ParseResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.TEMPORARY_EXPENSES.PARSE(accountBookId),
    options: {
      method: 'POST',
      body: JSON.stringify(data),
    },
  });
};
