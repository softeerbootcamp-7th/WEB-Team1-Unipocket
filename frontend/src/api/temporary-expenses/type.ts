export type UploadType = 'IMAGE' | 'FILE';

export interface PresignedUrlRequest {
  fileName: string;
  mimeType: string;
  uploadType: UploadType;
  tempExpenseMetaId?: number;
}

export interface PresignedUrlResponse {
  presignedUrl: string;
  s3Key: string;
  tempExpenseMetaId: number;
  expiresIn: number;
}

export interface ParseRequest {
  tempExpenseMetaId: number;
  s3Keys: string[];
}

export interface ParseResponse {
  taskId: string;
}

export type ParseStatus = 'PENDING' | 'PROCESSING' | 'SUCCESS' | 'FAIL';

export interface ParseStatusResponse {
  status: ParseStatus;
  progress?: number;
  metaId?: number;
  message?: string;
}
