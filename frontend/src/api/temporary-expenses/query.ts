import {
  queryOptions,
  useMutation,
  useQuery,
  useSuspenseQuery,
} from '@tanstack/react-query';
import { toast } from 'sonner';

import {
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
} from '@/api/temporary-expenses/api';
import type {
  BulkUpdateRequest,
  GetPresignedUrlRequest,
  StartParseRequest,
} from '@/api/temporary-expenses/type';
import { queryClient } from '@/main';

export const temporaryExpenseKeys = {
  all: ['temporaryExpenses'] as const,
  metas: (accountBookId: number) =>
    [...temporaryExpenseKeys.all, 'metas', accountBookId] as const,
  metaFiles: (accountBookId: number, metaId: number) =>
    [...temporaryExpenseKeys.all, 'metaFiles', accountBookId, metaId] as const,
  metaFileDetail: (accountBookId: number, metaId: number, fileId: number) =>
    [
      ...temporaryExpenseKeys.all,
      'metaFileDetail',
      accountBookId,
      metaId,
      fileId,
    ] as const,
  metaFileUrl: (accountBookId: number, metaId: number, fileId: number) =>
    [
      ...temporaryExpenseKeys.all,
      'metaFileUrl',
      accountBookId,
      metaId,
      fileId,
    ] as const,
};

/** 메타 목록 Query Options */
const metasQueryOptions = (accountBookId: number) =>
  queryOptions({
    queryKey: temporaryExpenseKeys.metas(accountBookId),
    queryFn: () => getMetas(accountBookId),
  });

/** 메타 목록 조회 */
const useGetMetasQuery = (accountBookId: number) =>
  useQuery(metasQueryOptions(accountBookId));

/** 메타 목록 조회 (Suspense) */
const useGetMetasSuspenseQuery = (accountBookId: number) =>
  useSuspenseQuery(metasQueryOptions(accountBookId));

/** 파일별 임시지출 Query Options */
const metaFilesQueryOptions = (accountBookId: number, metaId: number) =>
  queryOptions({
    queryKey: temporaryExpenseKeys.metaFiles(accountBookId, metaId),
    queryFn: () => getMetaFiles(accountBookId, metaId),
  });

/** 파일별 임시지출 조회 */
const useGetMetaFilesQuery = (accountBookId: number, metaId: number) =>
  useQuery(metaFilesQueryOptions(accountBookId, metaId));

/** 파일별 임시지출 조회 (Suspense) */
const useGetMetaFilesSuspenseQuery = (accountBookId: number, metaId: number) =>
  useSuspenseQuery(metaFilesQueryOptions(accountBookId, metaId));

/** 파일 단건 Query Options */
const metaFileDetailQueryOptions = (
  accountBookId: number,
  metaId: number,
  fileId: number,
) =>
  queryOptions({
    queryKey: temporaryExpenseKeys.metaFileDetail(
      accountBookId,
      metaId,
      fileId,
    ),
    queryFn: () => getMetaFileDetail(accountBookId, metaId, fileId),
  });

/** 파일 단건 조회 */
const useGetMetaFileDetailQuery = (
  accountBookId: number,
  metaId: number,
  fileId: number,
) => useQuery(metaFileDetailQueryOptions(accountBookId, metaId, fileId));

/** 파일 단건 조회 (Suspense) */
const useGetMetaFileDetailSuspenseQuery = (
  accountBookId: number,
  metaId: number,
  fileId: number,
) =>
  useSuspenseQuery(metaFileDetailQueryOptions(accountBookId, metaId, fileId));

/** 파일 열람 URL Query Options */
const metaFileUrlQueryOptions = (
  accountBookId: number,
  metaId: number,
  fileId: number,
) =>
  queryOptions({
    queryKey: temporaryExpenseKeys.metaFileUrl(accountBookId, metaId, fileId),
    queryFn: () => getMetaFileUrl(accountBookId, metaId, fileId),
    staleTime: 1000 * 60 * 4, // presigned URL 만료 전 재발급 (4분)
  });

/** 파일 열람 URL 발급 */
const useGetMetaFileUrlQuery = (
  accountBookId: number,
  metaId: number,
  fileId: number,
) => useQuery(metaFileUrlQueryOptions(accountBookId, metaId, fileId));

/** Presigned URL 발급 Mutation */
const usePresignedUrlMutation = (accountBookId: number) =>
  useMutation({
    mutationFn: (data: GetPresignedUrlRequest) =>
      getPresignedUrl(accountBookId, data),
    onError: () => {
      toast.error('업로드 URL 발급에 실패했어요.');
    },
  });

/** 파싱 시작 Mutation */
const useStartParseMutation = (accountBookId: number) =>
  useMutation({
    mutationFn: (data: StartParseRequest) => startParse(accountBookId, data),
    onError: () => {
      toast.error('파싱 시작에 실패했어요.');
    },
  });

/** 임시지출 확정 Mutation */
const useConfirmMetaMutation = (accountBookId: number) =>
  useMutation({
    mutationFn: (metaId: number) => confirmMeta(accountBookId, metaId),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: temporaryExpenseKeys.metas(accountBookId),
      });
      toast.success('임시지출이 확정됐어요.');
    },
    onError: () => {
      toast.error('임시지출 확정에 실패했어요.');
    },
  });

/** 임시지출 일괄 수정 Mutation */
const useBulkUpdateTempExpensesMutation = () =>
  useMutation({
    mutationFn: ({
      accountBookId,
      metaId,
      fileId,
      data,
    }: {
      accountBookId: number | string;
      metaId: number;
      fileId: number;
      data: BulkUpdateRequest;
    }) => bulkUpdateTempExpenses(accountBookId, metaId, fileId, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: temporaryExpenseKeys.metaFiles(
          variables.accountBookId as number,
          variables.metaId,
        ),
      });
      queryClient.invalidateQueries({
        queryKey: temporaryExpenseKeys.metaFileDetail(
          variables.accountBookId as number,
          variables.metaId,
          variables.fileId,
        ),
      });
      toast.success('임시지출이 수정됐어요.');
    },
    onError: () => {
      toast.error('임시지출 수정에 실패했어요.');
    },
  });

/** 메타 삭제 Mutation */
const useDeleteMetaMutation = (accountBookId: number) =>
  useMutation({
    mutationFn: (metaId: number) => deleteMeta(accountBookId, metaId),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: temporaryExpenseKeys.metas(accountBookId),
      });
      toast.success('임시지출 파일이 삭제됐어요.');
    },
    onError: () => {
      toast.error('임시지출 파일 삭제에 실패했어요.');
    },
  });

/** 임시지출 단건 삭제 Mutation */
const useDeleteTempExpenseMutation = (
  accountBookId: number,
  metaId: number,
  fileId: number,
) =>
  useMutation({
    mutationFn: (tempExpenseId: number) =>
      deleteTempExpense(accountBookId, metaId, fileId, tempExpenseId),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: temporaryExpenseKeys.metaFiles(accountBookId, metaId),
      });
      queryClient.invalidateQueries({
        queryKey: temporaryExpenseKeys.metaFileDetail(
          accountBookId,
          metaId,
          fileId,
        ),
      });
      toast.success('임시지출이 삭제됐어요.');
    },
    onError: () => {
      toast.error('임시지출 삭제에 실패했어요.');
    },
  });

export {
  useBulkUpdateTempExpensesMutation,
  useConfirmMetaMutation,
  useDeleteMetaMutation,
  useDeleteTempExpenseMutation,
  useGetMetaFileDetailQuery,
  useGetMetaFileDetailSuspenseQuery,
  useGetMetaFilesQuery,
  useGetMetaFilesSuspenseQuery,
  useGetMetaFileUrlQuery,
  useGetMetasQuery,
  useGetMetasSuspenseQuery,
  usePresignedUrlMutation,
  useStartParseMutation,
};
