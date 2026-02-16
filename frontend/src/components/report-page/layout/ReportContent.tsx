import type { PropsWithChildren, ReactNode } from 'react';

import { cn } from '@/lib/utils';

interface ChartContentProps extends PropsWithChildren {
  className?: string;
  isLoading?: boolean;
  skeleton?: ReactNode;
}

const ReportContent = ({
  children,
  className,
  isLoading = false,
  skeleton,
}: ChartContentProps) => {
  return (
    <div
      className={cn(
        'rounded-modal-8 bg-cool-neutral-99 flex flex-col p-8',
        className,
      )}
    >
      {isLoading ? skeleton : children}
    </div>
  );
};

export default ReportContent;
