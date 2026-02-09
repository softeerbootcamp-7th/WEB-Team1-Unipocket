import ReportBarRow from './ReportBarRow';

interface ReportBarListProps {
  data: {
    category: string;
    me: number;
    other: number;
  }[];
}

const ReportBarList = ({ data }: ReportBarListProps) => {
  const maxValue = Math.max(...data.flatMap((d) => [d.me, d.other]));

  return (
    <div className="flex flex-col gap-4.5">
      {data.map((item) => (
        <ReportBarRow key={item.category} {...item} maxValue={maxValue} />
      ))}
    </div>
  );
};

export default ReportBarList;
