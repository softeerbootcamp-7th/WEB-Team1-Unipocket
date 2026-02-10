import { useMemo } from 'react';
import clsx from 'clsx';

import ReportBarList from './ReportBarList';

const LEGEND_COLOR = {
  me: 'bg-primary-normal',
  other: 'bg-cool-neutral-95',
} as const;

interface ReportBarLegendProps {
  color: keyof typeof LEGEND_COLOR;
  label: string;
}

const ReportBarLegend = ({ color, label }: ReportBarLegendProps) => {
  return (
    <div className="flex items-center gap-1.5">
      <div className={clsx('h-2.5 w-2.5', LEGEND_COLOR[color])} />
      <span className="label1-normal-regular text-label-alternative">
        {label}
      </span>
    </div>
  );
};

interface ReportBarGraphProps {
  maxLabel: number;
  items: {
    categoryIndex: number;
    mySpentAmount: string;
    averageSpentAmount: string;
  }[];
}

const transformCategoryData = (items: ReportBarGraphProps['items']) => {
  return items.map((item) => ({
    categoryIndex: item.categoryIndex,
    me: Number(item.mySpentAmount),
    other: Number(item.averageSpentAmount),
  }));
};

const ReportBarGraph = ({ maxLabel, items }: ReportBarGraphProps) => {
  const data = useMemo(() => transformCategoryData(items), [items]);

  return (
    <div className="flex w-145.5 flex-col gap-3.5">
      <div className="flex justify-end gap-4">
        <ReportBarLegend label="나" color="me" />
        <ReportBarLegend label="다른 학생" color="other" />
      </div>
      <ReportBarList items={data} maxLabel={maxLabel} />
    </div>
  );
};

export default ReportBarGraph;
