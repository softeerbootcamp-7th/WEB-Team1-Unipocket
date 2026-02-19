import { useEffect, useRef, useState } from 'react';
import { toast } from 'sonner';

import type { UploadItem } from '@/components/upload/type';
import { uploadPolicy } from '@/components/upload/upload-box/useFileValidator';

const MAX_TOTAL = uploadPolicy.image.maxCount;

export const useImageUpload = () => {
  const [items, setItems] = useState<UploadItem[]>([]);
  const itemsRef = useRef<UploadItem[]>([]);

  useEffect(() => {
    itemsRef.current = items;
  }, [items]);

  useEffect(() => {
    return () => {
      itemsRef.current.forEach((item) => {
        if (item.url) {
          URL.revokeObjectURL(item.url);
        }
      });
    };
  }, []);

  const handleFilesSelected = (files: File[]) => {
    if (items.length + files.length > MAX_TOTAL) {
      toast.error(`최대 ${MAX_TOTAL}개까지 업로드할 수 있어요.`);
      return;
    }

    const newItems: UploadItem[] = files.map((file) => ({
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
    setItems((prevItems) => {
      const itemToRemove = prevItems.find((item) => item.id === id);
      if (itemToRemove?.url) {
        URL.revokeObjectURL(itemToRemove.url);
      }
      return prevItems.filter((item) => item.id !== id);
    });
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
