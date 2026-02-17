import { useState } from 'react';
import clsx from 'clsx';

import { Icons } from '@/assets';

export type UploadStatus = 'uploading' | 'done' | 'error';

export interface UploadImageItem {
  id: string;
  name: string;
  url?: string; // 업로드 완료 시 존재
  status: UploadStatus;
}

interface UploadImageProps {
  item: UploadImageItem;
  onRemove: (id: string) => void;
}

const UploadImage = ({ item, onRemove }: UploadImageProps) => {
  const [isHover, setIsHover] = useState(false);

  const isUploading = item.status === 'uploading';
  const fileExtension = item.name.split('.').pop()?.toUpperCase() || '';

  return (
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

      {!isUploading && isHover && (
        <Icons.CloseButton
          className="absolute top-1 right-0.5 size-6 cursor-pointer"
          style={{
            filter: 'drop-shadow(0 2px 6px rgba(0,0,0,0.12))',
          }}
          onClick={() => {
            onRemove(item.id);
          }}
        />
      )}
    </div>
  );
};

export default UploadImage;
