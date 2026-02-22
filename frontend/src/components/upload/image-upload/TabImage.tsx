import { Icons } from '@/assets';

interface TabImageProps {
  fileName: string;
  successCount: number;
  warningCount: number;
  hasNotification?: boolean;
  thumbnailUrl: string;
}

const TabImage = ({
  fileName,
  successCount,
  warningCount,
  hasNotification = false,
  thumbnailUrl,
}: TabImageProps) => {
  return (
    <div className="relative flex h-12 w-34.5 cursor-pointer items-center gap-2">
      <div className="border-line-normal-neutral flex h-12 w-9 items-center justify-center rounded-sm border">
        <img
          src={thumbnailUrl}
          alt={fileName}
          className="h-full w-full rounded-sm object-cover"
        />
      </div>
      {hasNotification && (
        <div className="bg-status-negative absolute -top-0.75 -left-0.75 h-1.5 w-1.5 rounded-full" />
      )}

      <div className="flex min-w-0 flex-col gap-1.5">
        <span className="caption1-medium text-label-normal truncate">
          {fileName}
        </span>
        <div className="caption1-medium flex h-4 items-center gap-1.5">
          {successCount > 0 && (
            <div className="flex items-center gap-0.5">
              <Icons.Checkmark className="size-4" />
              <span className="text-status-positive">{successCount}건</span>
            </div>
          )}
          {warningCount > 0 && (
            <div className="flex items-center gap-0.5">
              <Icons.Warning className="size-4" />
              <span className="text-status-cautionary">{warningCount}건</span>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default TabImage;
