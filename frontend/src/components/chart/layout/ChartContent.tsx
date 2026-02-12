import type { PropsWithChildren, ReactNode } from 'react';

import { cn } from '@/lib/utils';

interface ChartContentProps extends PropsWithChildren {
  className?: string;
  isPreview?: boolean;
  skeleton?: ReactNode;
}

const ChartContent = ({
  children,
  className,
  isPreview = false,
  skeleton,
}: ChartContentProps) => {
  return (
    <div
      className={cn(
        'rounded-modal-8 bg-background-alternative flex flex-1 justify-between p-5',
        className,
      )}
    >
      {isPreview ? skeleton : children}
    </div>
  );
};

export default ChartContent;
