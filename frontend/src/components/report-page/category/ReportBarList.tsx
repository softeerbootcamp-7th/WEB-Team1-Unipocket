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
  const maxValue = 150;

  return (
    <div className="relative h-125.25 w-full">
      <VerticalGrid steps={6} maxValue={maxValue} />
      <div className="relative z-10 flex flex-col gap-4.5">
        {data.map((item) => (
          <ReportBarRow key={item.category} {...item} />
        ))}
      </div>
    </div>
  );
};

export default ReportBarList;
