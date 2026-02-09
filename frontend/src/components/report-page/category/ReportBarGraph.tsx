import { mockData } from './mock';
import ReportBarList from './ReportBarList';

interface ReportBarLegendProps {
  color: string;
  label: string;
}

const ReportBarLegend = ({ color, label }: ReportBarLegendProps) => {
  return (
    <div className="flex items-center gap-1.5">
      <div className={`bg-${color} h-2.5 w-2.5`} />
      <span className="label1-normal-regular text-label-alternative">
        {label}
      </span>
    </div>
  );
};

const ReportBarGraph = () => {
  return (
    <div className="flex w-145.5 flex-col gap-3.5">
      <div className="flex justify-end gap-4">
        <ReportBarLegend label="나" color="primary-normal" />
        <ReportBarLegend label="다른 학생" color="cool-neutral-95" />
      </div>
      <ReportBarList data={mockData} />
    </div>
  );
};

export default ReportBarGraph;
