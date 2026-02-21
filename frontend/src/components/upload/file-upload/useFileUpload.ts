import { useEffect, useRef, useState } from 'react';

import { UPLOAD_STATUS, type UploadItem } from '@/components/upload/type';

export const useFileUpload = () => {
  const [item, setItem] = useState<UploadItem | null>(null);
  const itemRef = useRef<UploadItem | null>(null);

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

  const handleFilesSelected = (files: File[]) => {
    if (files.length !== 1) return;

    const file = files[0];
    const newItem: UploadItem = {
      id: crypto.randomUUID(),
      name: file.name,
      status: UPLOAD_STATUS.UPLOADING,
    };

    setItem(newItem);

    // @TODO: 업로드 로직 구현 후 테스트 코드 제거
    // 테스트용: 2초 뒤 done 처리
    setTimeout(() => {
      setItem((prev) =>
        prev?.id === newItem.id
          ? { ...prev, status: UPLOAD_STATUS.UPLOADED }
          : prev,
      );
    }, 2000);
  };

  const removeItem = () => {
    if (item?.url) {
      URL.revokeObjectURL(item.url);
    }
    setItem(null);
  };

  const isReady = item !== null && item.status === UPLOAD_STATUS.UPLOADED;

  return {
    item,
    setItem,
    handleFilesSelected,
    removeItem,
    isReady,
  };
};
