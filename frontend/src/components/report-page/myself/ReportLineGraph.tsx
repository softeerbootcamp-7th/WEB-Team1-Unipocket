import ComparisonLineChart from '@/components/report-page/myself/ComparisonLineChart';
import ReportLegend from '@/components/report-page/ReportLegend';
import VerticalGrid from '@/components/report-page/VerticalGrid';

interface ReportLineGraphProps {
  thisMonthItem: { date: string; cumulatedAmount: string }[];
  prevMonthItem: { date: string; cumulatedAmount: string }[];
}
const ReportLineGraph = ({
  thisMonthItem,
  prevMonthItem,
}: ReportLineGraphProps) => {
  const maxDays = Math.max(thisMonthItem.length, prevMonthItem.length);
  const thisMonthLastDay = thisMonthItem.length;
  const labels = ['1', `${thisMonthLastDay}`, `${maxDays}`];

  return (
    <div className="flex flex-col gap-4">
      <div className="flex justify-end gap-4">
        <ReportLegend label="1월" color="me" variant="line" />
        <ReportLegend label="2월" color="other" variant="line" />
      </div>
      <div className="relative">
        <ComparisonLineChart
          thisMonth={thisMonthItem}
          prevMonth={prevMonthItem}
        />
        <VerticalGrid steps={2} labels={labels} />
      </div>
    </div>
  );
};

export default ReportLineGraph;
