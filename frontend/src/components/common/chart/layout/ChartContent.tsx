import type { PropsWithChildren, ReactNode } from 'react';
import clsx from 'clsx';

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
      className={clsx(
        'rounded-modal-8 bg-background-alternative flex justify-between px-8 py-4',
        className,
      )}
    >
      {isLoading ? skeleton : children}
    </div>
  );
};

export default ChartContent;
