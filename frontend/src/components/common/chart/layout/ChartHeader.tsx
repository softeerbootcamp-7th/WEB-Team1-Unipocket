import type { PropsWithChildren } from 'react';

interface ChartHeaderProps extends PropsWithChildren {
  title: string;
}

const ChartHeader = ({ title, children }: ChartHeaderProps) => {
  return (
    <div className="flex items-center justify-between px-2.5">
      <span className="body2-normal-medium text-label-neutral">{title}</span>
      <div className="flex items-center gap-1.5">{children}</div>
    </div>
  );
};

export default ChartHeader;
