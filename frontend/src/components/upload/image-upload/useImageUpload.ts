import { useEffect, useRef, useState } from 'react';
import { toast } from 'sonner';

import type { SnackbarStatus } from '@/components/common/Snackbar';
import { UPLOAD_STATUS, type UploadItem } from '@/components/upload/type';
import { uploadPolicy } from '@/components/upload/upload-box/useFileValidator';

import { ENDPOINTS } from '@/api/config/endpoint';
import { getPresignedUrl, startParse } from '@/api/temporary-expenses/api';

const MAX_TOTAL = uploadPolicy.image.maxCount;

export const useImageUpload = (accountBookId: number) => {
  const [items, setItems] = useState<UploadItem[]>([]);
  const [parseSnackbar, setParseSnackbar] = useState<{
    isOpen: boolean;
    status: SnackbarStatus;
    description?: string;
  }>({
    isOpen: false,
    status: 'default',
  });
  const itemsRef = useRef<UploadItem[]>([]);
  const eventSourcesRef = useRef<Record<string, EventSource>>({});
  const metaIdRef = useRef<number | undefined>(undefined);
  const completedRef = useRef<Record<string, boolean>>({});

  useEffect(() => {
    itemsRef.current = items;
  }, [items]);

  // unmount 시 모든 SSE 정리
  useEffect(() => {
    const eventSources = eventSourcesRef.current;
    const items = itemsRef.current;
    return () => {
      Object.values(eventSources).forEach((es) => es.close());

      items.forEach((item) => {
        if (item.url) {
          URL.revokeObjectURL(item.url);
        }
      });
    };
  }, []);

  const handleFilesSelected = async (files: File[]) => {
    if (itemsRef.current.length + files.length > MAX_TOTAL) {
      toast.error(`최대 ${MAX_TOTAL}개까지 업로드할 수 있어요.`);
      return;
    }

    for (const file of files) {
      const id = crypto.randomUUID();

      const previewUrl = URL.createObjectURL(file);

      setItems((prev) => [
        ...prev,
        {
          id,
          name: file.name,
          url: previewUrl,
          status: UPLOAD_STATUS.UPLOADING,
        },
      ]);

      try {
        // 1. presigned 발급
        const presigned = await getPresignedUrl(accountBookId, {
          fileName: file.name,
          mimeType: file.type,
          uploadType: 'IMAGE',
          tempExpenseMetaId: metaIdRef.current,
        });

        // 메타 ID 재사용 (1:N 업로드)
        metaIdRef.current = presigned.tempExpenseMetaId;

        setItems((prev) =>
          prev.map((item) =>
            item.id === id ? { ...item, s3Key: presigned.s3Key } : item,
          ),
        );

        // 2️. S3 업로드
        const response = await fetch(presigned.presignedUrl, {
          method: 'PUT',
          body: file,
          headers: {
            'Content-Type': file.type,
          },
        });

        if (!response.ok) {
          throw new Error(`S3 upload failed: ${response.status}`);
        }

        setItems((prev) =>
          prev.map((item) =>
            item.id === id ? { ...item, status: UPLOAD_STATUS.UPLOADED } : item,
          ),
        );
      } catch {
        setItems((prev) =>
          prev.map((item) =>
            item.id === id ? { ...item, status: UPLOAD_STATUS.ERROR } : item,
          ),
        );
        toast.error('파일 업로드에 실패했어요.');
      }
    }
  };

  const startParsing = async () => {
    const uploadedItems = itemsRef.current.filter(
      (item) => item.status === UPLOAD_STATUS.UPLOADED,
    );

    if (uploadedItems.length === 0) return false;

    try {
      const s3Keys = uploadedItems.map((item) => {
        return item.s3Key!;
      });

      const parse = await startParse(accountBookId, {
        tempExpenseMetaId: metaIdRef.current!,
        s3Keys,
      });

      // 3. 파싱 시작과 동시에 상태 변경 (SSE로 성공/실패 업데이트 예정)
      setItems((prev) =>
        prev.map((item) =>
          item.status === UPLOAD_STATUS.UPLOADED
            ? { ...item, status: UPLOAD_STATUS.PARSING, taskId: parse.taskId }
            : item,
        ),
      );

      // 4. SSE 연결
      connectSSE(parse.taskId);
      setParseSnackbar({
        isOpen: true,
        status: 'loading',
        description: '0%',
      });
      return true;
    } catch {
      toast.error('파일 분석 요청에 실패했어요.');
      return false;
    }
  };

  const connectSSE = (taskId: string) => {
    const baseUrl = import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, '');
    if (!baseUrl) return;

    const endpoint = ENDPOINTS.TEMPORARY_EXPENSES.PARSE_STATUS(
      accountBookId,
      taskId,
    );

    const url = `${baseUrl}/${endpoint}`;

    const eventSource = new EventSource(url, {
      withCredentials: true,
    });

    eventSourcesRef.current[taskId] = eventSource;

    const closeEventSource = () => {
      eventSource.close();
      delete eventSourcesRef.current[taskId];
    };

    const markTaskAsParsed = () => {
      if (completedRef.current[taskId]) return;
      completedRef.current[taskId] = true;

      setItems((prev) =>
        prev.map((item) =>
          item.taskId === taskId
            ? { ...item, status: UPLOAD_STATUS.PARSED }
            : item,
        ),
      );

      setParseSnackbar({
        isOpen: true,
        status: 'success',
        description: '100%',
      });
    };

    const handleProgressValue = (progress?: number) => {
      if (typeof progress !== 'number') return;

      const normalizedProgress = Math.max(0, Math.min(100, progress));

      if (normalizedProgress >= 100) {
        markTaskAsParsed();
        closeEventSource();
        return;
      }

      setParseSnackbar((prev) => ({
        ...prev,
        isOpen: true,
        description: `${normalizedProgress}%`,
      }));
    };

    const parseEventData = (rawData: string) => {
      return JSON.parse(rawData) as {
        progress?: number;
      };
    };

    eventSource.onmessage = (event) => {
      try {
        const parsed = parseEventData(event.data);
        handleProgressValue(parsed.progress);
      } catch {
        closeEventSource();
      }
    };

    eventSource.addEventListener('progress', (event) => {
      try {
        const parsed = parseEventData((event as MessageEvent).data);
        handleProgressValue(parsed.progress);
      } catch {
        closeEventSource();
      }
    });

    eventSource.addEventListener('complete', (event) => {
      try {
        const parsed = parseEventData((event as MessageEvent).data);
        if (typeof parsed.progress === 'number') {
          handleProgressValue(parsed.progress);
          return;
        }
        markTaskAsParsed();
        closeEventSource();
      } catch {
        closeEventSource();
      }
    });

    eventSource.onerror = () => {
      if (!completedRef.current[taskId]) {
        toast.error('분석 중 연결이 끊어졌어요. 다시 시도해주세요.');
      }

      setParseSnackbar({
        isOpen: false,
        status: 'default',
      });

      closeEventSource();
    };
  };

  const closeParseSnackbar = () => {
    setParseSnackbar({ isOpen: false, status: 'default' });
  };

  const removeItem = (id: string) => {
    setItems((prev) => {
      const itemToRemove = prev.find((item) => item.id === id);

      if (itemToRemove?.taskId) {
        const isLastItemForTask = !prev.some(
          (item) => item.id !== id && item.taskId === itemToRemove.taskId,
        );
        if (isLastItemForTask) {
          const eventSource = eventSourcesRef.current[itemToRemove.taskId];
          if (eventSource) {
            eventSource.close();
            delete eventSourcesRef.current[itemToRemove.taskId];
          }
        }
      }

      if (itemToRemove?.url) {
        URL.revokeObjectURL(itemToRemove.url);
      }

      const nextItems = prev.filter((item) => item.id !== id);

      if (nextItems.length === 0) {
        metaIdRef.current = undefined;
      }

      return nextItems;
    });
  };

  const isAllUploaded =
    items.length > 0 &&
    items.every((item) => item.status === UPLOAD_STATUS.UPLOADED);

  return {
    items,
    startParsing,
    handleFilesSelected,
    removeItem,
    isAllUploaded,
    parseSnackbar,
    closeParseSnackbar,
  };
};
