import ReportBarRow from '@/components/report-page/category/ReportBarRow';
import VerticalGrid from '@/components/report-page/VerticalGrid';

import type { CategoryId } from '@/types/category';

interface ReportBarListProps {
  maxLabel: number;
  items: {
    categoryIndex: CategoryId;
    me: number;
    other: number;
  }[];
}
const ReportBarList = ({ items, maxLabel }: ReportBarListProps) => {
  const steps = 6;
  const labels = Array.from(
    { length: steps + 1 },
    (_, i) => (maxLabel / steps) * i,
  );

  return (
    <div className="relative h-125.25 pt-4.75">
      <VerticalGrid steps={steps} labels={labels} className="left-14.75" />
      <div className="relative z-10 flex flex-col gap-4.5">
        {items.map((item) => (
          <ReportBarRow
            key={item.categoryIndex}
            {...item}
            maxLabel={maxLabel}
          />
        ))}
      </div>
    </div>
  );
};

export default ReportBarList;
