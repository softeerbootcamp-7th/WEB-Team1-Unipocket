import type { PropsWithChildren, ReactNode } from 'react';

import { cn } from '@/lib/utils';

interface ChartContentProps extends PropsWithChildren {
  className?: string;
  isPreview?: boolean;
  isEmpty?: boolean;
  skeleton?: ReactNode;
}

const ChartContent = ({
  children,
  className,
  isPreview = false,
  isEmpty = false,
  skeleton,
}: ChartContentProps) => {
  const showSkeleton = isPreview || isEmpty;

  return (
    <div
      className={cn(
        'rounded-modal-8 bg-background-alternative relative flex flex-1 justify-between p-5',
        className,
      )}
    >
      {showSkeleton ? skeleton : children}
      {isEmpty && (
        <div className="absolute inset-0 flex items-center justify-center">
          <span className="body2-normal-medium text-label-neutral text-center">
            데이터가 부족합니다.
            <br />
            지출 내역을 추가해주세요.
          </span>
        </div>
      )}
    </div>
  );
};

export default ChartContent;
