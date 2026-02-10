import mockData from '../mock.json';
import ReportBarRow from './ReportBarRow';
import VerticalGrid from './VerticalGrid';

interface ReportBarListProps {
  data: {
    category: string;
    me: number;
    other: number;
  }[];
}
const ReportBarList = ({ data }: ReportBarListProps) => {
  const maxCategoryValue = Number(
    mockData.compareByCategory.maxCategoryValue,
  );

  return (
    <div className="relative h-125.25 w-full">
      <VerticalGrid steps={6} maxValue={maxCategoryValue} />
      <div className="relative z-10 flex flex-col gap-4.5">
        {data.map((item) => (
          <ReportBarRow
            key={item.category}
            {...item}
            maxValue={maxCategoryValue}
          />
        ))}
      </div>
    </div>
  );
};

export default ReportBarList;
