import { useState } from 'react';
import clsx from 'clsx';

import ImagePreviewModal from '@/components/upload/image-upload/ImagePreviewModal';
import { type UploadItem } from '@/components/upload/type';

import { Icons } from '@/assets';

interface UploadImageProps {
  item: UploadItem;
  onRemove: (id: string) => void;
}

const UploadImage = ({ item, onRemove }: UploadImageProps) => {
  const [isHover, setIsHover] = useState(false);
  const [isPreviewOpen, setIsPreviewOpen] = useState(false);

  const isUploading = item.status === 'uploading';
  const fileExtension = item.name.split('.').pop()?.toUpperCase() || '';

  return (
    <>
      <div
        className="relative flex h-45.25 w-26 flex-col items-center justify-center gap-4 overflow-hidden p-3"
        onMouseEnter={() => setIsHover(true)}
        onMouseLeave={() => setIsHover(false)}
      >
        <div className="border-line-normal-normal h-26.75 w-20 rounded-lg border">
          <div className="h-full w-full overflow-hidden rounded-lg">
            {item.url && (
              <img
                src={item.url}
                alt={item.name}
                className={clsx(
                  'h-full w-full object-cover',
                  isUploading && 'blur-sm',
                )}
              />
            )}
          </div>
        </div>
        <div className="flex w-full flex-col gap-1 text-center">
          <span className="caption1-medium text-label-normal truncate">
            {item.name}
          </span>
          <span className="caption2-medium text-label-alternative">
            {isUploading ? '업로드 중' : fileExtension}
          </span>
        </div>

        {isHover && (
          <Icons.CloseButton
            className="absolute top-1 right-[2.5px] size-6 cursor-pointer"
            style={{
              filter: 'drop-shadow(0 2px 6px rgba(0,0,0,0.12))',
            }}
            onClick={() => {
              onRemove(item.id);
            }}
          />
        )}

        {!isUploading && isHover && (
          <div
            className="rounded-modal-6 bg-label-alternative absolute bottom-16.5 left-4 flex size-6 cursor-pointer items-center justify-center backdrop-blur-xs"
            onClick={(e) => {
              e.stopPropagation();
              setIsPreviewOpen(true);
            }}
          >
            <Icons.Expand className="text-inverse-label size-4" />
          </div>
        )}
      </div>

      {item.url && (
        <ImagePreviewModal
          isOpen={isPreviewOpen}
          imageUrl={item.url}
          imageName={item.name}
          onClose={() => setIsPreviewOpen(false)}
        />
      )}
    </>
  );
};

export default UploadImage;
