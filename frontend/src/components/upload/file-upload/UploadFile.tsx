import { type UploadItem } from '@/components/upload/type';

import { Icons } from '@/assets';

interface UploadFileProps {
  item: UploadItem;
  onRemove: (id: string) => void;
}

const UploadFile = ({ item, onRemove }: UploadFileProps) => {
  const { name, status } = item;
  const isUploading = status === 'uploading';

  return (
    <div className="border-line-normal-neutral flex items-center justify-between rounded-2xl border p-4">
      <div className="flex flex-col gap-1">
        <span className="body2-normal-bold text-label-normal">{name}</span>

        {isUploading && (
          <div className="flex items-center gap-1">
            <span className="label2-medium text-label-alternative">
              업로드 중...
            </span>
            <Icons.Loading className="text-label-assistive size-3 animate-spin" />
          </div>
        )}

        {status === 'done' && (
          <div className="flex items-center gap-1">
            <Icons.Checkmark className="size-4" />
            <span className="label2-medium text-status-positive">
              업로드 완료
            </span>
          </div>
        )}

        {status === 'error' && (
          <div className="flex items-center gap-1">
            <Icons.Alert className="size-4" />
            <span className="label2-medium text-status-negative">
              업로드 실패
            </span>
          </div>
        )}
      </div>

      <button onClick={() => onRemove(item.id)}>
        <Icons.Trash className="text-label-alternative size-5 cursor-pointer" />
      </button>
    </div>
  );
};

export default UploadFile;
