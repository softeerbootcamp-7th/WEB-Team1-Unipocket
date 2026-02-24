import { useEffect, useRef, useState } from 'react';
import { toast } from 'sonner';

import type { SnackbarStatus } from '@/components/common/Snackbar';

import { ENDPOINTS } from '@/api/config/endpoint';

interface ParseSSECallbacks {
  /** 개별 파일 단위 파싱 완료 시 호출 (image: fileKey 매칭 처리용) */
  onFileComplete?: (fileKey: string) => void;
  /** 100% 완료 시 추가 처리 (ex. file: setIsParsing(false)) */
  onComplete?: () => void;
  /** SSE 에러 시 추가 처리 (ex. file: 아이템 상태 ERROR 처리) */
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

    setParseSnackbar({ isOpen: true, status: 'loading', description: '0%' });

    const handleProgressValue = (data: {
      progress?: number;
      fileKey?: string;
      code?: string;
    }) => {
      const { progress, fileKey, code } = data;
      if (typeof progress !== 'number') return;

      const normalizedProgress = Math.max(0, Math.min(100, progress));

      // 개별 파일 완료 (image: fileKey 매칭)
      if (fileKey && code === 'SUCCESS') {
        callbacks?.onFileComplete?.(fileKey);
      }

      // 전체 완료 (100%)
      if (normalizedProgress >= 100) {
        if (completedRef.current[taskId]) return;
        completedRef.current[taskId] = true;

        setParsedMetaId(metaId);
        setParseSnackbar({
          isOpen: true,
          status: 'success',
          description: '100%',
        });
        callbacks?.onComplete?.();
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
