import type { PropsWithChildren, ReactNode } from 'react';

import { cn } from '@/lib/utils';

interface ChartContentProps extends PropsWithChildren {
  className?: string;
  isLoading?: boolean;
  skeleton?: ReactNode;
}

const ChartContent = ({
  children,
  className,
  isLoading = false,
  skeleton,
}: ChartContentProps) => {
  return (
    <div
      className={cn(
        'rounded-modal-8 bg-background-alternative flex justify-between p-5',
        className,
      )}
    >
      {isLoading ? skeleton : children}
    </div>
  );
};

export default ChartContent;
