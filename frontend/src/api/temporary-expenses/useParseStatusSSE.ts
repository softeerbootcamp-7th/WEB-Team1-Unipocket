import { useEffect, useRef, useState } from 'react';

import { ENDPOINTS } from '@/api/config/endpoint';
import type { GetParseStatusResponse } from '@/api/temporary-expenses/type';

const BASE_URL = import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, '');

interface UseParseStatusSSEProps {
  accountBookId: number;
  taskId: string | null;
  enabled?: boolean;
}

export const useParseStatusSSE = ({
  accountBookId,
  taskId,
  enabled = true,
}: UseParseStatusSSEProps) => {
  const [data, setData] = useState<GetParseStatusResponse | null>(null);
  const [isConnected, setIsConnected] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const eventSourceRef = useRef<EventSource | null>(null);

  // taskId 변경 시 상태 초기화
  const [prevTaskId, setPrevTaskId] = useState(taskId);
  if (taskId !== prevTaskId) {
    setPrevTaskId(taskId);
    setData(null);
    setError(null);
    setIsConnected(false);
  }

  // SSE 연결 effect
  useEffect(() => {
    if (!enabled || !taskId || !BASE_URL) return;

    // 기존 연결 정리
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
      eventSourceRef.current = null;
    }

    const endpoint = ENDPOINTS.TEMPORARY_EXPENSES.PARSE_STATUS(
      accountBookId,
      taskId,
    );

    const url = `${BASE_URL}/${endpoint}`;

    const eventSource = new EventSource(url, {
      withCredentials: true, // 쿠키 인증 시 필요
    });

    eventSourceRef.current = eventSource;

    eventSource.onopen = () => {
      setIsConnected(true);
      setError(null);
    };

    eventSource.onmessage = (event) => {
      try {
        const parsed: GetParseStatusResponse = JSON.parse(event.data);
        setData(parsed);

        if (parsed.status === 'SUCCESS' || parsed.status === 'FAIL') {
          eventSource.close();
          eventSourceRef.current = null;
          setIsConnected(false);
        }
      } catch (err) {
        setError(
          err instanceof Error ? err : new Error('Failed to parse SSE message'),
        );
      }
    };

    eventSource.onerror = () => {
      setIsConnected(false);
      setError(new Error('SSE connection error'));

      if (eventSourceRef.current) {
        eventSourceRef.current.close();
        eventSourceRef.current = null;
      }
    };

    return () => {
      if (eventSourceRef.current) {
        eventSourceRef.current.close();
        eventSourceRef.current = null;
      }
      setIsConnected(false);
    };
  }, [accountBookId, taskId, enabled]);

  return {
    data,
    isConnected,
    error: !BASE_URL ? new Error('API base URL is not defined') : error,
  };
};
