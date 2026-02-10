import ReportBarRow from './ReportBarRow';
import VerticalGrid from './VerticalGrid';

interface ReportBarListProps {
  maxLabel: number;
  items: {
    categoryIndex: number;
    me: number;
    other: number;
  }[];
}
const ReportBarList = ({ items, maxLabel }: ReportBarListProps) => {
  return (
    <div className="relative h-125.25 w-full pt-4.75">
      <VerticalGrid steps={6} maxLabel={maxLabel} />
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
