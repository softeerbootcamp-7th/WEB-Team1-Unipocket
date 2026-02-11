import ComparisonLineChart from '@/components/report-page/myself/ComparisonLineChart';
import ReportLegend from '@/components/report-page/ReportLegend';

interface ReportLineGraphProps {
  thisMonthItem: { date: string; cumulatedAmount: string }[];
  prevMonthItem: { date: string; cumulatedAmount: string }[];
}
const ReportLineGraph = ({
  thisMonthItem,
  prevMonthItem,
}: ReportLineGraphProps) => {
  return (
    <div>
      <div className="flex justify-end gap-4">
        <ReportLegend label="1월" color="me" variant="line" />
        <ReportLegend label="2월" color="other" variant="line" />
      </div>
      <ComparisonLineChart
        thisMonth={thisMonthItem}
        prevMonth={prevMonthItem}
      />
    </div>
  );
};

export default ReportLineGraph;
