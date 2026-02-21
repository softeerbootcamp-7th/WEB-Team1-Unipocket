import { customFetch } from '@/api/config/client';
import { ENDPOINTS } from '@/api/config/endpoint';
import type {
  BulkUpdateRequest,
  BulkUpdateResponse,
  GetMetaFileDetailResponse,
  GetMetaFilesResponse,
  GetMetaFileUrlResponse,
  GetMetasResponse,
  GetPresignedUrlRequest,
  GetPresignedUrlResponse,
  ParseTaskResponse,
  StartParseRequest,
} from '@/api/temporary-expenses/type';

// 임시지출 업로드 URL 발급
const getPresignedUrl = (
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

// 임시지출 파싱 시작
const startParse = (
  accountBookId: number,
  data: StartParseRequest,
): Promise<ParseTaskResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.TEMPORARY_EXPENSES.PARSE(accountBookId),
    options: {
      method: 'POST',
      body: JSON.stringify(data),
    },
  });
};

// 임시지출 확정
const confirmMeta = (
  accountBookId: number,
  metaId: number,
): Promise<ParseTaskResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.TEMPORARY_EXPENSES.CONFIRM(accountBookId, metaId),
    options: { method: 'POST' },
  });
};

// 임시지출 일괄 수정
const bulkUpdateTempExpenses = (
  accountBookId: number,
  metaId: number,
  fileId: number,
  data: BulkUpdateRequest,
): Promise<BulkUpdateResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.TEMPORARY_EXPENSES.BULK_UPDATE(
      accountBookId,
      metaId,
      fileId,
    ),
    options: {
      method: 'PATCH',
      body: JSON.stringify(data),
    },
  });
};

// 임시지출 메타 목록 조회
const getMetas = (accountBookId: number): Promise<GetMetasResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.TEMPORARY_EXPENSES.METAS(accountBookId),
    options: { method: 'GET' },
  });
};

// 메타 내부 파일별 임시지출 조회
const getMetaFiles = (
  accountBookId: number,
  metaId: number,
): Promise<GetMetaFilesResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.TEMPORARY_EXPENSES.META_FILES(accountBookId, metaId),
    options: { method: 'GET' },
  });
};

// 메타 내부 파일 단건 임시지출 조회
const getMetaFileDetail = (
  accountBookId: number,
  metaId: number,
  fileId: number,
): Promise<GetMetaFileDetailResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.TEMPORARY_EXPENSES.META_FILE_DETAIL(
      accountBookId,
      metaId,
      fileId,
    ),
    options: { method: 'GET' },
  });
};

// 메타 내부 파일 열람 URL 발급
const getMetaFileUrl = (
  accountBookId: number,
  metaId: number,
  fileId: number,
): Promise<GetMetaFileUrlResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.TEMPORARY_EXPENSES.META_FILE_URL(
      accountBookId,
      metaId,
      fileId,
    ),
    options: { method: 'GET' },
  });
};

// 임시지출 메타 삭제
const deleteMeta = (accountBookId: number, metaId: number): Promise<void> => {
  return customFetch({
    endpoint: ENDPOINTS.TEMPORARY_EXPENSES.DELETE_META(accountBookId, metaId),
    options: { method: 'DELETE' },
  });
};

// 임시지출 단건 삭제
const deleteTempExpense = (
  accountBookId: number,
  metaId: number,
  fileId: number,
  tempExpenseId: number,
): Promise<void> => {
  return customFetch({
    endpoint: ENDPOINTS.TEMPORARY_EXPENSES.DELETE_TEMP_EXPENSE(
      accountBookId,
      metaId,
      fileId,
      tempExpenseId,
    ),
    options: { method: 'DELETE' },
  });
};

export {
  bulkUpdateTempExpenses,
  confirmMeta,
  deleteMeta,
  deleteTempExpense,
  getMetaFileDetail,
  getMetaFiles,
  getMetaFileUrl,
  getMetas,
  getPresignedUrl,
  startParse,
};
