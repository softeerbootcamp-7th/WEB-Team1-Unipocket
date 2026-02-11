import type { ComponentProps } from 'react';

import { cn } from '@/lib/utils';

// props 추가 시 type -> interface 변경 필요
type ChartContainerProps = ComponentProps<'div'>;

const ChartContainer = ({
  children,
  className,
  ...props
}: ChartContainerProps) => {
  return (
    <div
      className={cn(
        'rounded-modal-16 bg-background-normal shadow-semantic-subtle flex w-67 flex-col gap-2.5 p-2 pt-4',
        className,
      )}
      {...props}
    >
      {children}
    </div>
  );
};

export default ChartContainer;
