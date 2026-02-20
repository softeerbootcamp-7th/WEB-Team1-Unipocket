import { customFetch } from '@/api/config/client';
import { ENDPOINTS } from '@/api/config/endpoint';
import type {
  GetPresignedUrlRequest,
  GetPresignedUrlResponse,
  StartParseRequest,
  StartParseResponse,
} from '@/api/temporary-expenses/type';

export const getPresignedUrl = (
  accountBookId: number,
  data: GetPresignedUrlRequest,
): Promise<GetPresignedUrlResponse> => {
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
  data: StartParseRequest,
): Promise<StartParseResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.TEMPORARY_EXPENSES.PARSE(accountBookId),
    options: {
      method: 'POST',
      body: JSON.stringify(data),
    },
  });
};
