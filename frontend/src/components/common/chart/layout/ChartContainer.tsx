import type { ComponentProps } from 'react';
import clsx from 'clsx';

// props 추가 시 type -> interface 변경 필요
type ChartContainerProps = ComponentProps<'div'>;

const ChartContainer = ({
  children,
  className,
  ...props
}: ChartContainerProps) => {
  return (
    <div
      className={clsx(
        'rounded-modal-16 bg-background-normal shadow-semantic-subtle flex flex-col gap-2.5 p-2 pt-4',
        className,
      )}
      {...props}
    >
      {children}
    </div>
  );
};

export default ChartContainer;
