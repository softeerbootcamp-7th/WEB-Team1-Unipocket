import { useEffect } from 'react';
import { clsx } from 'clsx';

import Divider from '@/components/common/Divider';
import { useModalContext } from '@/components/modal/useModalContext';
import UploadGallery from '@/components/upload/image-upload/UploadGallery';
import { useImageUpload } from '@/components/upload/image-upload/useImageUpload';
import UploadBox from '@/components/upload/upload-box/UploadBox';

const ImageUploadContent = () => {
  const { setActionReady } = useModalContext();
  const { items, handleFilesSelected, removeItem, isAllUploaded } =
    useImageUpload();
  const hasItems = items.length > 0;

  useEffect(() => {
    setActionReady(isAllUploaded);
  }, [isAllUploaded, setActionReady]);

  return (
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
              <UploadGallery items={items} onRemove={removeItem} />
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default ImageUploadContent;
