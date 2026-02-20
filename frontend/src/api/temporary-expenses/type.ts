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

export type ParseStatus = 'PENDING' | 'PROCESSING' | 'SUCCESS' | 'FAIL';

export interface GetParseStatusResponse {
  status: ParseStatus;
  progress?: number;
  metaId?: number;
  message?: string;
}
