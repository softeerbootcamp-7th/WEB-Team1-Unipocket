import { useState } from 'react';

import type { UploadImageItem } from '@/components/upload/image-upload/UploadImage';
import { uploadPolicy } from '@/components/upload/upload-box/useFileValidator';

const MAX_TOTAL = uploadPolicy.image.maxCount;

export const useImageUpload = () => {
  const [items, setItems] = useState<UploadImageItem[]>([]);

  const handleFilesSelected = (files: File[]) => {
    const remaining = MAX_TOTAL - items.length;

    if (remaining <= 0) {
      //@TODO: Toast로 변경
      alert(`최대 ${MAX_TOTAL}개까지 업로드할 수 있어요.`);
      return;
    }

    const limitedFiles = files.slice(0, remaining);

    const newItems: UploadImageItem[] = limitedFiles.map((file) => ({
      id: crypto.randomUUID(),
      name: file.name,
      url: URL.createObjectURL(file),
      status: 'uploading',
    }));

    setItems((prev) => [...prev, ...newItems]);

    // @TODO: s3 업로드 로직 구현 후 테스트 코드 제거
    // 테스트용: 2초 뒤 done 처리
    setTimeout(() => {
      setItems((prev) =>
        prev.map((item) =>
          newItems.find((n) => n.id === item.id)
            ? { ...item, status: 'done' }
            : item,
        ),
      );
    }, 2000);
  };

  const removeItem = (id: string) => {
    setItems((prev) => prev.filter((item) => item.id !== id));
  };

  const isAllUploaded =
    items.length > 0 && items.every((item) => item.status === 'done');

  return {
    items,
    setItems,
    handleFilesSelected,
    removeItem,
    isAllUploaded,
  };
};
