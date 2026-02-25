import type { PropsWithChildren, ReactNode } from 'react';

import { CHART_MESSAGES } from '@/components/chart/message';

import { cn } from '@/lib/utils';

interface ChartContentProps extends PropsWithChildren {
  className?: string;
  isPreview?: boolean;
  isEmpty?: boolean;
  skeleton?: ReactNode;
  emptyMessage?: string;
}

const ChartContent = ({
  children,
  className,
  isPreview = false,
  isEmpty = false,
  emptyMessage = CHART_MESSAGES.DEFAULT_EMPTY,
  skeleton,
}: ChartContentProps) => {
  const showSkeleton = isPreview || isEmpty;

  return (
    <div
      className={cn(
        'rounded-modal-8 bg-background-alternative relative flex flex-1 justify-between overflow-hidden p-5',
        className,
      )}
    >
      {showSkeleton ? skeleton : children}
      {isEmpty && (
        <div className="absolute inset-0 flex items-center justify-center">
          <span className="body2-normal-medium text-label-alternative text-center whitespace-pre-line">
            {emptyMessage}
          </span>
        </div>
      )}
    </div>
  );
};

export default ChartContent;
