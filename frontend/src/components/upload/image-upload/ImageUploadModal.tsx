import { useState } from 'react';
import { clsx } from 'clsx';

import Divider from '@/components/common/Divider';
import Modal from '@/components/modal/Modal';
import UploadGallery from '@/components/upload/image-upload/UploadGallery';
import type { UploadImageItem } from '@/components/upload/image-upload/UploadImage';
import UploadBox from '@/components/upload/upload-box/UploadBox';
import { uploadPolicy } from '@/components/upload/upload-box/useFileValidator';

interface ImageUploadModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const MAX_TOTAL = uploadPolicy.image.maxCount;

const ImageUploadModal = ({ isOpen, onClose }: ImageUploadModalProps) => {
  const [items, setItems] = useState<UploadImageItem[]>([]);
  const hasItems = items.length > 0;

  const handleFilesSelected = (files: File[]) => {
    if (items.length + files.length > MAX_TOTAL) {
      //@TODO: Toast로 변경
      alert(`최대 ${MAX_TOTAL}개까지 업로드할 수 있어요.`);
      return;
    }

    const newItems: UploadImageItem[] = files.map((file) => ({
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

  const handleClose = () => {
    setItems([]);
    onClose();
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      onAction={() => {}}
      className="px-8 pb-4"
      confirmButton={{ label: '결과 확인' }}
    >
      <div className="flex w-242 flex-col gap-6">
        <div className="flex flex-col">
          <div className="flex flex-col gap-3">
            <h2 className="headline1-bold text-label-normal">
              영수증 / 은행 앱 사진 업로드
            </h2>
            <span className="body1-normal-medium text-label-alternative">
              종이 영수증이나 모바일 결제 화면 캡처본을 업로드해 주세요.
            </span>
          </div>
        </div>
        <div
          className={clsx(
            'mb-2.5 flex h-121.5 gap-5 transition-all',
            hasItems ? 'justify-start' : 'justify-center',
          )}
        >
          <div
            className={clsx(
              'transition-all duration-200',
              hasItems ? 'w-md' : 'w-full',
            )}
          >
            <UploadBox type="image" onFilesSelected={handleFilesSelected} />
          </div>

          {hasItems && (
            <>
              <Divider style="vertical" />
              <div className="w-md">
                <UploadGallery
                  items={items}
                  onRemove={(id) =>
                    setItems((prev) => prev.filter((item) => item.id !== id))
                  }
                />
              </div>
            </>
          )}
        </div>
      </div>
    </Modal>
  );
};

export default ImageUploadModal;
