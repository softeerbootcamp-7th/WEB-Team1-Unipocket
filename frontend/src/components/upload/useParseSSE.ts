import { useEffect, useRef, useState } from 'react';
import { toast } from 'sonner';

import type { SnackbarStatus } from '@/components/common/Snackbar';

import { ENDPOINTS } from '@/api/config/endpoint';

interface ParseSSECallbacks {
  // 개별 파일 파싱 성공 시 호출 (image: fileKey 매칭 처리용)
  onFileComplete?: (fileKey: string) => void;
  // 개별 파일 파싱 실패 시 호출 (image: 해당 아이템 ERROR 처리용)
  onFileFailed?: (fileKey: string) => void;
  // 전체 완료(100%) + 하나 이상 성공 시 호출
  onComplete?: () => void;
  // SSE 연결 에러 또는 전체 실패(100% + 성공 0건) 시 호출
  onError?: () => void;
}

export const useParseSSE = (accountBookId: number) => {
  const [parseSnackbar, setParseSnackbar] = useState<{
    isOpen: boolean;
    status: SnackbarStatus;
    description?: string;
  }>({
    isOpen: false,
    status: 'default',
  });
  const [parsedMetaId, setParsedMetaId] = useState<number | undefined>(
    undefined,
  );

  const eventSourcesRef = useRef<Record<string, EventSource>>({});
  const completedRef = useRef<Record<string, boolean>>({});
  const successCountRef = useRef<Record<string, number>>({});

  useEffect(() => {
    const eventSources = eventSourcesRef.current;
    return () => {
      Object.values(eventSources).forEach((es) => es.close());
    };
  }, []);

  const disconnect = (taskId: string) => {
    const es = eventSourcesRef.current[taskId];
    if (es) {
      es.close();
      delete eventSourcesRef.current[taskId];
    }
    delete completedRef.current[taskId];
    delete successCountRef.current[taskId];
  };

  const disconnectAll = () => {
    Object.keys(eventSourcesRef.current).forEach(disconnect);
  };

  const connect = (
    taskId: string,
    metaId: number,
    callbacks?: ParseSSECallbacks,
  ) => {
    const baseUrl = import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, '');
    if (!baseUrl) return;

    const endpoint = ENDPOINTS.TEMPORARY_EXPENSES.PARSE_STATUS(
      accountBookId,
      taskId,
    );
    const eventSource = new EventSource(`${baseUrl}/${endpoint}`, {
      withCredentials: true,
    });

    eventSourcesRef.current[taskId] = eventSource;
    successCountRef.current[taskId] = 0;

    setParseSnackbar({ isOpen: true, status: 'loading', description: '0%' });

    const handleProgressValue = (data: {
      progress?: number;
      fileKey?: string;
      code?: string;
    }) => {
      const { progress, fileKey, code } = data;
      if (typeof progress !== 'number') return;

      const normalizedProgress = Math.max(0, Math.min(100, progress));

      // 개별 파일 성공
      if (fileKey && code === 'SUCCESS') {
        successCountRef.current[taskId] =
          (successCountRef.current[taskId] ?? 0) + 1;
        callbacks?.onFileComplete?.(fileKey);
      }

      // 개별 파일 실패
      if (fileKey && code !== 'SUCCESS' && code != null) {
        callbacks?.onFileFailed?.(fileKey);
      }

      // 전체 완료 (100%)
      if (normalizedProgress >= 100) {
        if (completedRef.current[taskId]) return;
        completedRef.current[taskId] = true;

        const hasAnySuccess = (successCountRef.current[taskId] ?? 0) > 0;

        if (hasAnySuccess) {
          setParsedMetaId(metaId);
          setParseSnackbar({
            isOpen: true,
            status: 'success',
            description: '100%',
          });
          callbacks?.onComplete?.();
        } else {
          setParseSnackbar({ isOpen: false, status: 'default' });
          toast.error('모든 파일 분석에 실패했어요. 다시 시도해주세요.');
          callbacks?.onError?.();
        }

        disconnect(taskId);
        return;
      }

      // 진행률 업데이트
      setParseSnackbar((prev) => ({
        ...prev,
        isOpen: true,
        status: 'loading',
        description: `${normalizedProgress}%`,
      }));
    };

    eventSource.addEventListener('progress', (event) => {
      try {
        const parsed = JSON.parse((event as MessageEvent).data);
        handleProgressValue(parsed);
      } catch {
        toast.error(
          '분석 진행 상태를 가져오는 중 문제가 발생했어요. 다시 시도해주세요.',
        );
        setParseSnackbar({ isOpen: false, status: 'default' });
        callbacks?.onError?.();
        disconnect(taskId);
      }
    });

    eventSource.onerror = () => {
      if (!completedRef.current[taskId]) {
        toast.error('분석 중 연결이 끊어졌어요. 다시 시도해주세요.');
        setParseSnackbar({ isOpen: false, status: 'default' });
        callbacks?.onError?.();
      }
      disconnect(taskId);
    };
  };

  const closeParseSnackbar = () => {
    setParseSnackbar({ isOpen: false, status: 'default' });
  };

  const resetParseState = () => {
    setParsedMetaId(undefined);
    setParseSnackbar({ isOpen: false, status: 'default' });
  };

  return {
    parseSnackbar,
    parsedMetaId,
    connect,
    disconnect,
    disconnectAll,
    closeParseSnackbar,
    resetParseState,
  };
};
