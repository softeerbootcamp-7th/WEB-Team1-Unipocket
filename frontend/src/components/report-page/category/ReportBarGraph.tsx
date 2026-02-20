import ReportBarList from '@/components/report-page/category/ReportBarList';
import ReportLegend from '@/components/report-page/ReportLegend';

import { type AnalysisCategoryItem } from '@/api/account-books/type';
interface ReportBarGraphProps {
  maxLabel: number;
  items: AnalysisCategoryItem[];
}

const ReportBarGraph = ({ maxLabel, items }: ReportBarGraphProps) => {
  return (
    <div className="flex w-full flex-col gap-3.5">
      <div className="flex justify-end gap-4">
        <ReportLegend label="나" color="primary" />
        <ReportLegend label="다른 학생" color="secondary" />
      </div>
      <ReportBarList items={items} maxLabel={maxLabel} />
    </div>
  );
};

export default ReportBarGraph;
