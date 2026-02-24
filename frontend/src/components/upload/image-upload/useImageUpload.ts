import { useEffect, useRef, useState } from 'react';
import { toast } from 'sonner';

import { UPLOAD_STATUS, type UploadItem } from '@/components/upload/type';
import { uploadPolicy } from '@/components/upload/upload-box/useFileValidator';
import { useParseSSE } from '@/components/upload/useParseSSE';

import { getPresignedUrl, startParse } from '@/api/temporary-expenses/api';

const MAX_TOTAL = uploadPolicy.image.maxCount;

export const useImageUpload = (accountBookId: number) => {
  const [items, setItems] = useState<UploadItem[]>([]);
  const itemsRef = useRef<UploadItem[]>([]);
  const metaIdRef = useRef<number | undefined>(undefined);

  const {
    parseSnackbar,
    parsedMetaId,
    connect,
    disconnect,
    disconnectAll,
    closeParseSnackbar,
    resetParseState,
  } = useParseSSE(accountBookId);

  useEffect(() => {
    itemsRef.current = items;
  }, [items]);

  useEffect(() => {
    return () => {
      itemsRef.current.forEach((item) => {
        if (item.url) URL.revokeObjectURL(item.url);
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
          headers: { 'Content-Type': file.type },
        });

        if (!response.ok) throw new Error(`S3 upload failed`);

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
      const s3Keys = uploadedItems.map((item) => item.s3Key!);
      const parse = await startParse(accountBookId, {
        tempExpenseMetaId: metaIdRef.current!,
        s3Keys,
      });

      // 3. 파싱 시작과 동시에 상태 변경
      setItems((prev) =>
        prev.map((item) =>
          item.status === UPLOAD_STATUS.UPLOADED
            ? { ...item, status: UPLOAD_STATUS.PARSING, taskId: parse.taskId }
            : item,
        ),
      );

      // 4. SSE 연결
      connect(parse.taskId, metaIdRef.current!, {
        onFileComplete: (fileKey) =>
          setItems((prev) =>
            prev.map((item) =>
              item.s3Key === fileKey
                ? { ...item, status: UPLOAD_STATUS.PARSED }
                : item,
            ),
          ),
        onFileFailed: (fileKey) =>
          setItems((prev) =>
            prev.map((item) =>
              item.s3Key === fileKey
                ? { ...item, status: UPLOAD_STATUS.ERROR }
                : item,
            ),
          ),
        onComplete: () =>
          setItems((prev) =>
            prev.map((item) =>
              item.taskId === parse.taskId
                ? { ...item, status: UPLOAD_STATUS.PARSED }
                : item,
            ),
          ),
      });
      return true;
    } catch {
      toast.error('파일 분석 요청에 실패했어요.');
      return false;
    }
  };

  const removeItem = (id: string) => {
    setItems((prev) => {
      const itemToRemove = prev.find((item) => item.id === id);
      if (itemToRemove?.taskId) {
        const isLastItemForTask = !prev.some(
          (item) => item.id !== id && item.taskId === itemToRemove.taskId,
        );
        if (isLastItemForTask) disconnect(itemToRemove.taskId);
      }
      if (itemToRemove?.url) URL.revokeObjectURL(itemToRemove.url);

      const nextItems = prev.filter((item) => item.id !== id);
      if (nextItems.length === 0) metaIdRef.current = undefined;
      return nextItems;
    });
  };

  const isAllUploaded =
    items.length > 0 &&
    items.every((item) => item.status === UPLOAD_STATUS.UPLOADED);

  const clearItems = () => {
    itemsRef.current.forEach((item) => {
      if (item.url) URL.revokeObjectURL(item.url);
    });
    setItems([]);
    metaIdRef.current = undefined;
  };

  const resetAll = () => {
    clearItems();
    disconnectAll();
    resetParseState();
  };

  return {
    items,
    startParsing,
    handleFilesSelected,
    removeItem,
    isAllUploaded,
    parseSnackbar,
    closeParseSnackbar,
    clearItems,
    resetAll,
    parsedMetaId,
  };
};
