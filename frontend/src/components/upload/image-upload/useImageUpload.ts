import { useEffect, useRef, useState } from 'react';
import { toast } from 'sonner';

import type { UploadItem } from '@/components/upload/type';
import { UPLOAD_STATUS } from '@/components/upload/type';
import { uploadPolicy } from '@/components/upload/upload-box/useFileValidator';

import { ENDPOINTS } from '@/api/config/endpoint';
import {
  usePresignedUrlMutation,
  useStartParseMutation,
} from '@/api/temporary-expenses/query';

const MAX_TOTAL = uploadPolicy.image.maxCount;

export const useImageUpload = (accountBookId: number) => {
  const [items, setItems] = useState<UploadItem[]>([]);
  const itemsRef = useRef<UploadItem[]>([]);
  const eventSourcesRef = useRef<Record<string, EventSource>>({});
  const metaIdRef = useRef<number | undefined>(undefined);

  const presignedMutation = usePresignedUrlMutation(accountBookId);
  const startParseMutation = useStartParseMutation(accountBookId);

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
        const presigned = await presignedMutation.mutateAsync({
          fileName: file.name,
          mimeType: file.type,
          uploadType: 'IMAGE',
          tempExpenseMetaId: metaIdRef.current,
        });

        // 메타 ID 재사용 (1:N 업로드)
        metaIdRef.current = presigned.tempExpenseMetaId;

        // 2️. S3 업로드
        await fetch(presigned.presignedUrl, {
          method: 'PUT',
          body: file,
          headers: {
            'Content-Type': file.type,
          },
        });

        // 3️. parse 시작
        const parse = await startParseMutation.mutateAsync({
          tempExpenseMetaId: presigned.tempExpenseMetaId,
          s3Keys: [presigned.s3Key],
        });

        // 4️. taskId 저장
        setItems((prev) =>
          prev.map((item) =>
            item.id === id ? { ...item, taskId: parse.taskId } : item,
          ),
        );

        // 5️. SSE 연결
        connectSSE(id, parse.taskId);
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

  const connectSSE = (itemId: string, taskId: string) => {
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

    eventSourcesRef.current[itemId] = eventSource;

    eventSource.onmessage = (event) => {
      try {
        const parsed = JSON.parse(event.data);

        if (parsed.status === 'SUCCESS') {
          setItems((prev) =>
            prev.map((item) =>
              item.id === itemId
                ? { ...item, status: UPLOAD_STATUS.DONE }
                : item,
            ),
          );
          eventSource.close();
          delete eventSourcesRef.current[itemId];
        }

        if (parsed.status === 'FAIL') {
          setItems((prev) =>
            prev.map((item) =>
              item.id === itemId
                ? { ...item, status: UPLOAD_STATUS.ERROR }
                : item,
            ),
          );
          eventSource.close();
          delete eventSourcesRef.current[itemId];
        }
      } catch {
        eventSource.close();
        delete eventSourcesRef.current[itemId];
      }
    };

    eventSource.onerror = () => {
      eventSource.close();
      delete eventSourcesRef.current[itemId];
    };
  };

  const removeItem = (id: string) => {
    const eventSource = eventSourcesRef.current[id];
    if (eventSource) {
      eventSource.close();
      delete eventSourcesRef.current[id];
    }

    setItems((prev) => {
      const itemToRemove = prev.find((item) => item.id === id);
      if (itemToRemove?.url) {
        URL.revokeObjectURL(itemToRemove.url);
      }
      return prev.filter((item) => item.id !== id);
    });
  };

  const isAllUploaded =
    items.length > 0 &&
    items.every((item) => item.status === UPLOAD_STATUS.DONE);

  return {
    items,
    handleFilesSelected,
    removeItem,
    isAllUploaded,
  };
};
