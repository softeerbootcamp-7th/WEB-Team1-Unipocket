export type UploadType = 'IMAGE' | 'FILE';

export interface GetPresignedUrlRequest {
  fileName: string;
  mimeType: string;
  uploadType: UploadType;
  tempExpenseMetaId?: number;
}

export interface GetPresignedUrlResponse {
  presignedUrl: string;
  s3Key: string;
  tempExpenseMetaId: number;
  expiresIn: number;
}

export interface StartParseRequest {
  tempExpenseMetaId: number;
  s3Keys: string[];
}

export interface StartParseResponse {
  taskId: string;
}

export const PARSE_STATUS = {
  PENDING: 'PENDING',
  PROCESSING: 'PROCESSING',
  SUCCESS: 'SUCCESS',
  FAIL: 'FAIL',
} as const;

export type ParseStatus = (typeof PARSE_STATUS)[keyof typeof PARSE_STATUS];

export interface GetParseStatusResponse {
  status: ParseStatus;
  progress?: number;
  metaId?: number;
  message?: string;
}
