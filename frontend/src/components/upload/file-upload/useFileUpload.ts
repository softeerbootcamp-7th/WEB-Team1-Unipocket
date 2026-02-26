import { useEffect, useRef, useState } from 'react';
import { toast } from 'sonner';

import { UPLOAD_STATUS, type UploadItem } from '@/components/upload/type';
import { useParseSSE } from '@/components/upload/useParseSSE';

import { getPresignedUrl, startParse } from '@/api/temporary-expenses/api';

export const useFileUpload = (accountBookId: number) => {
  const [item, setItem] = useState<UploadItem | null>(null);
  const [isParsing, setIsParsing] = useState(false);
  const itemRef = useRef<UploadItem | null>(null);
  const metaIdRef = useRef<number | undefined>(undefined);

  const { connect, disconnect, closeSnackbar } = useParseSSE(accountBookId);

  useEffect(() => {
    itemRef.current = item;
  }, [item]);

  useEffect(() => {
    return () => {
      if (itemRef.current?.url) {
        URL.revokeObjectURL(itemRef.current.url);
      }
    };
  }, []);

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
      connect(parse.taskId, metaIdRef.current, 'file', {
        onComplete: () => setIsParsing(false),
        onError: () => {
          setItem((prev) =>
            prev?.taskId === parse.taskId
              ? { ...prev, status: UPLOAD_STATUS.ERROR }
              : prev,
          );
          setIsParsing(false);
        },
      });
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

  const clearItemAfterParseStart = () => {
    if (itemRef.current?.url) {
      URL.revokeObjectURL(itemRef.current.url);
    }
    setItem(null);
    metaIdRef.current = undefined;
  };

  const removeItem = () => {
    const current = itemRef.current;
    if (current?.taskId) {
      disconnect(current.taskId);
      closeSnackbar(current.taskId);
    }
    if (current?.url) URL.revokeObjectURL(current.url);
    setItem(null);
    metaIdRef.current = undefined;
    setIsParsing(false);
  };

  const clearItem = () => {
    if (itemRef.current?.url) URL.revokeObjectURL(itemRef.current.url);
    setItem(null);
    metaIdRef.current = undefined;
    setIsParsing(false);
  };

  const isReady = item !== null && item.status === UPLOAD_STATUS.UPLOADED;

  return {
    item,
    handleFilesSelected,
    removeItem,
    isReady,
    startParsing,
    isParsing,
    clearItemAfterParseStart,
    clearItem,
  };
};
