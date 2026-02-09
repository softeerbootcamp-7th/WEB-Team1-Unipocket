import ReportBar from './ReportBar';

interface ReportBarRowProps {
  category: string;
  me: number;
  other: number;
  maxValue: number;
}

const ReportBarRow = ({ category, me, other }: ReportBarRowProps) => {
  return (
    <div className="flex items-center gap-4">
      <span className="flex w-12 label1-normal-medium text-label-neutral justify-end">
        {category}
      </span>

      <div className="flex flex-col justify-between h-10 w-full">
        <ReportBar value={me} variant="me" />
        <ReportBar value={other} variant="other" />
      </div>
    </div>
  );
};

export default ReportBarRow;
