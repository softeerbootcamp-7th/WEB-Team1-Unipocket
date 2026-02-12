import ComparisonLineChart from '@/components/report-page/myself/ComparisonLineChart';
import ReportLegend from '@/components/report-page/ReportLegend';
import VerticalGrid from '@/components/report-page/VerticalGrid';

interface ReportLineGraphProps {
  thisMonthCount: number;
  lastMonthCount: number;
  maxValue: number;
  thisMonthItem: { date: string; cumulatedAmount: string }[];
  prevMonthItem: { date: string; cumulatedAmount: string }[];
}
const ReportLineGraph = ({
  thisMonthCount,
  lastMonthCount,
  maxValue,
  thisMonthItem,
  prevMonthItem,
}: ReportLineGraphProps) => {
  const maxDays = Math.max(thisMonthCount, lastMonthCount);
  const thisMonthLastDay = thisMonthCount;

  const positions = [0, ((thisMonthLastDay - 1) / (maxDays - 1)) * 100, 100];
  const labels = ['1일', `${thisMonthLastDay}일`, `${maxDays}일`];

  return (
    <div className="flex flex-col gap-4">
      <div className="flex justify-end gap-4">
        <ReportLegend label="1월" color="me" variant="line" />
        <ReportLegend label="2월" color="other" variant="line" />
      </div>
      <div className="relative h-42.5">
        <VerticalGrid positions={positions} labels={labels} />
        <div className="relative z-10 pt-1.5">
          <ComparisonLineChart
            maxValue={maxValue}
            thisMonth={thisMonthItem}
            prevMonth={prevMonthItem}
          />
        </div>
      </div>
    </div>
  );
};

export default ReportLineGraph;
