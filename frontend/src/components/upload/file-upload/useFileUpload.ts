import { useEffect, useRef, useState } from 'react';
import { toast } from 'sonner';

import type { SnackbarStatus } from '@/components/common/Snackbar';
import { UPLOAD_STATUS, type UploadItem } from '@/components/upload/type';

import { ENDPOINTS } from '@/api/config/endpoint';
import { getPresignedUrl, startParse } from '@/api/temporary-expenses/api';

export const useFileUpload = (accountBookId: number) => {
  const [item, setItem] = useState<UploadItem | null>(null);
  const [isParsing, setIsParsing] = useState(false);
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
  const itemRef = useRef<UploadItem | null>(null);
  const metaIdRef = useRef<number | undefined>(undefined);
  const eventSourceRef = useRef<EventSource | null>(null);

  useEffect(() => {
    itemRef.current = item;
  }, [item]);

  useEffect(() => {
    return () => {
      if (eventSourceRef.current) {
        eventSourceRef.current.close();
        eventSourceRef.current = null;
      }
      if (itemRef.current?.url) {
        URL.revokeObjectURL(itemRef.current.url);
      }
    };
  }, []);

  const closeEventSource = () => {
    if (!eventSourceRef.current) return;
    eventSourceRef.current.close();
    eventSourceRef.current = null;
  };

  const connectSSE = (taskId: string, metaId: number) => {
    const baseUrl = import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, '');
    if (!baseUrl) return;

    const endpoint = ENDPOINTS.TEMPORARY_EXPENSES.PARSE_STATUS(
      accountBookId,
      taskId,
    );
    const eventSource = new EventSource(`${baseUrl}/${endpoint}`, {
      withCredentials: true,
    });

    eventSourceRef.current = eventSource;

    const handleProgressValue = (data: { progress?: number }) => {
      const { progress } = data;
      if (typeof progress !== 'number') return;

      const normalizedProgress = Math.max(0, Math.min(100, progress));

      if (normalizedProgress >= 100) {
        setIsParsing(false);
        setParsedMetaId(metaId);
        setParseSnackbar({
          isOpen: true,
          status: 'success',
          description: '100%',
        });
        closeEventSource();
        return;
      }

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
        setItem((prev) =>
          prev?.taskId === taskId
            ? { ...prev, status: UPLOAD_STATUS.ERROR }
            : prev,
        );
        setIsParsing(false);
        setParseSnackbar({ isOpen: false, status: 'default' });
        toast.error(
          '분석 진행 상태를 가져오는 중 문제가 발생했어요. 다시 시도해주세요.',
        );
        closeEventSource();
      }
    });

    eventSource.onerror = () => {
      setItem((prev) =>
        prev?.taskId === taskId
          ? { ...prev, status: UPLOAD_STATUS.ERROR }
          : prev,
      );
      setIsParsing(false);
      setParseSnackbar({ isOpen: false, status: 'default' });
      toast.error('분석 중 연결이 끊어졌어요. 다시 시도해주세요.');
      closeEventSource();
    };
  };

  const handleFilesSelected = async (files: File[]) => {
    if (files.length !== 1) return;

    const file = files[0];
    const id = crypto.randomUUID();
    const mimeType = file.type || 'application/octet-stream';

    setItem({
      id,
      name: file.name,
      status: UPLOAD_STATUS.UPLOADING,
    });

    try {
      const presigned = await getPresignedUrl(accountBookId, {
        fileName: file.name,
        mimeType,
        uploadType: 'DOCS',
        tempExpenseMetaId: metaIdRef.current,
      });

      metaIdRef.current = presigned.tempExpenseMetaId;

      setItem((prev) =>
        prev?.id === id ? { ...prev, s3Key: presigned.s3Key } : prev,
      );

      const response = await fetch(presigned.presignedUrl, {
        method: 'PUT',
        body: file,
        headers: { 'Content-Type': mimeType },
      });

      if (!response.ok) {
        throw new Error('S3 upload failed');
      }

      setItem((prev) =>
        prev?.id === id ? { ...prev, status: UPLOAD_STATUS.UPLOADED } : prev,
      );
    } catch {
      setItem((prev) =>
        prev?.id === id ? { ...prev, status: UPLOAD_STATUS.ERROR } : prev,
      );
      toast.error('파일 업로드에 실패했어요.');
    }
  };

  const startParsing = async () => {
    const current = itemRef.current;
    if (
      !current ||
      current.status !== UPLOAD_STATUS.UPLOADED ||
      !current.s3Key ||
      !metaIdRef.current ||
      isParsing
    ) {
      return false;
    }

    setIsParsing(true);
    setItem((prev) =>
      prev ? { ...prev, status: UPLOAD_STATUS.PARSING } : prev,
    );

    try {
      const parse = await startParse(accountBookId, {
        tempExpenseMetaId: metaIdRef.current,
        s3Keys: [current.s3Key],
      });

      setItem((prev) =>
        prev
          ? { ...prev, status: UPLOAD_STATUS.PARSING, taskId: parse.taskId }
          : prev,
      );
      setParseSnackbar({
        isOpen: true,
        status: 'loading',
        description: '0%',
      });
      connectSSE(parse.taskId, metaIdRef.current);
      return true;
    } catch {
      setItem((prev) =>
        prev ? { ...prev, status: UPLOAD_STATUS.ERROR } : prev,
      );
      setIsParsing(false);
      toast.error('파일 분석 요청에 실패했어요.');
      return false;
    }
  };

  const closeParseSnackbar = () => {
    setParseSnackbar({ isOpen: false, status: 'default' });
  };

  const clearItemAfterParseStart = () => {
    if (itemRef.current?.url) {
      URL.revokeObjectURL(itemRef.current.url);
    }
    setItem(null);
  };

  const removeItem = () => {
    closeEventSource();
    if (item?.url) {
      URL.revokeObjectURL(item.url);
    }
    setItem(null);
    metaIdRef.current = undefined;
    setParsedMetaId(undefined);
    setIsParsing(false);
    setParseSnackbar({ isOpen: false, status: 'default' });
  };

  const clearItem = () => {
    closeEventSource();
    if (itemRef.current?.url) {
      URL.revokeObjectURL(itemRef.current.url);
    }
    setItem(null);
    metaIdRef.current = undefined;
    setParsedMetaId(undefined);
    setIsParsing(false);
    setParseSnackbar({ isOpen: false, status: 'default' });
  };

  const isReady = item !== null && item.status === UPLOAD_STATUS.UPLOADED;

  return {
    item,
    handleFilesSelected,
    removeItem,
    isReady,
    startParsing,
    isParsing,
    parseSnackbar,
    closeParseSnackbar,
    parsedMetaId,
    clearItemAfterParseStart,
    clearItem,
  };
};
