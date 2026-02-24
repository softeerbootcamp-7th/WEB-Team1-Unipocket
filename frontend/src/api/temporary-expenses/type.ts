import type { CategoryId } from '@/types/category';

import type { CurrencyCode } from '@/data/country/currencyCode';

export type UploadType = 'IMAGE' | 'DOCS';

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

export interface ParseTaskResponse {
  taskId: string;
  totalFiles: number;
  statusUrl: string;
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

// 메타 목록
export interface TempExpenseMeta {
  tempExpenseMetaId: number;
  createdAt: string;
  fileCount: number;
  totalExpenses: number;
  normalCount: number;
  incompleteCount: number;
  abnormalCount: number;
}

export interface GetMetasResponse {
  metas: TempExpenseMeta[];
}

// 파일별 임시지출
export interface TempExpense {
  tempExpenseId: number;
  tempExpenseMetaId: number;
  fileId: number;
  merchantName: string | null;
  category: CategoryId;
  localCountryCode: CurrencyCode | null;
  localCurrencyAmount: number | null;
  baseCountryCode: CurrencyCode;
  baseCurrencyAmount: number;
  memo: string | null;
  occurredAt: string | null;
  status: 'NORMAL' | 'INCOMPLETE' | 'ABNORMAL';
  cardLastFourDigits: string | null;
}

export interface TempExpenseFile {
  fileId: number;
  s3Key: string;
  fileType: string;
  expenses: TempExpense[];
}

export interface GetMetaFilesResponse {
  tempExpenseMetaId: number;
  createdAt: string;
  files: TempExpenseFile[];
}

export type GetMetaFileDetailResponse = TempExpenseFile;

export interface GetMetaFileUrlResponse {
  presignedUrl: string;
  expiresInSeconds: number;
}

export type BulkUpdateTempExpenseItem = Partial<TempExpense> & {
  tempExpenseId: number;
};

export interface BulkUpdateRequest {
  items: BulkUpdateTempExpenseItem[];
}

export interface BulkUpdateResult {
  tempExpenseId: number;
  reason: string;
  updated: TempExpense;
}

export interface BulkUpdateResponse {
  totalRequested: number;
  successCount: number;
  failedCount: number;
  results: BulkUpdateResult[];
}
