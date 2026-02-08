import type { PropsWithChildren } from 'react';
import clsx from 'clsx';

interface ChartHeaderProps extends PropsWithChildren {
  title: string;
}

const ChartHeader = ({ title, children }: ChartHeaderProps) => {
  return (
    <div className="flex items-center justify-between px-2.5">
      <span className={clsx('body2-normal-medium text-label-neutral')}>
        {title}
      </span>
      <div className="flex items-center gap-1.5">{children}</div>
    </div>
  );
};

export default ChartHeader;
