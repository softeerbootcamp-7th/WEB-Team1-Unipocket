import { useGetMetaFileUrlQuery } from '@/api/temporary-expenses/query';
import { Icons } from '@/assets';

interface FileImageProps {
  accountBookId: number;
  metaId: number;
  fileId: number;
  fileName: string;
  successCount?: number;
  warningCount?: number;
  hasIssue?: boolean;
  variant?: 'tab' | 'preview';
}

const FileImage = ({
  accountBookId,
  metaId,
  fileId,
  fileName,
  successCount = 0,
  warningCount = 0,
  hasIssue = false,
  variant = 'tab',
}: FileImageProps) => {
  const { data } = useGetMetaFileUrlQuery(accountBookId, metaId, fileId);
  const thumbnailUrl = data?.presignedUrl;

  // preview
  if (variant === 'preview') {
    return (
      <div className="border-line-normal-neutral aspect-400/533 h-133.25 w-full max-w-100 rounded-2xl border">
        {thumbnailUrl ? (
          <img
            src={thumbnailUrl}
            alt={fileName}
            className="h-full w-full rounded-2xl object-contain"
          />
        ) : (
          <div className="h-full w-full animate-pulse rounded-2xl bg-gray-200" />
        )}
      </div>
    );
  }

  // tab
  return (
    <div className="relative flex h-12 w-34.5 cursor-pointer items-center gap-2">
      <div className="border-line-normal-neutral flex h-12 w-9 shrink-0 items-center justify-center rounded-sm border">
        {thumbnailUrl ? (
          <img
            src={thumbnailUrl}
            alt={fileName}
            className="h-full w-full rounded-sm object-cover"
          />
        ) : (
          <div className="h-full w-full animate-pulse rounded-sm bg-gray-200" />
        )}
      </div>

      {hasIssue && (
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

export default FileImage;
