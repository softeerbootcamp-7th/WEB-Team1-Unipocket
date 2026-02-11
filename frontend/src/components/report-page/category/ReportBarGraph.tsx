import { useMemo } from 'react';

import ReportBarList from '@/components/report-page/category/ReportBarList';
import ReportLegend from '@/components/report-page/ReportLegend';

import type { CategoryId } from '@/types/category';

interface ReportBarGraphProps {
  maxLabel: number;
  items: {
    categoryIndex: CategoryId;
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
        <ReportLegend label="나" color="me" />
        <ReportLegend label="다른 학생" color="other" />
      </div>
      <ReportBarList items={data} maxLabel={maxLabel} />
    </div>
  );
};

export default ReportBarGraph;
