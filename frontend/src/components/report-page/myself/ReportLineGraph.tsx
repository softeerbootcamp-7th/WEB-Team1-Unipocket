import ComparisonLineChart from '@/components/report-page/myself/ComparisonLineChart';
import ReportLegend from '@/components/report-page/ReportLegend';
import VerticalGrid from '@/components/report-page/VerticalGrid';

interface ReportLineGraphProps {
  thisMonthLabel: string;
  lastMonthLabel: string;
  thisMonthCount: number;
  lastMonthCount: number;
  maxValue: number;
  thisMonthItem: { date: string; cumulatedAmount: string }[];
  lastMonthItem: { date: string; cumulatedAmount: string }[];
}
const ReportLineGraph = ({
  thisMonthLabel,
  lastMonthLabel,
  thisMonthCount,
  lastMonthCount,
  maxValue,
  thisMonthItem,
  lastMonthItem,
}: ReportLineGraphProps) => {
  const maxDays = Math.max(thisMonthCount, lastMonthCount);
  const thisMonthLastDay = thisMonthCount;

  const positions = [0, ((thisMonthLastDay - 1) / (maxDays - 1)) * 100, 100];
  const labels = ['1일', `${thisMonthLastDay}일`, `${maxDays}일`];

  return (
    <div className="flex flex-col gap-4">
      <div className="flex justify-end gap-4">
        <ReportLegend label={thisMonthLabel} color="primary" variant="line" />
        <ReportLegend label={lastMonthLabel} color="secondary" variant="line" />
      </div>
      <div className="relative h-42">
        <VerticalGrid positions={positions} labels={labels} />
        <div className="relative z-10 pt-2.5">
          <ComparisonLineChart
            thisMonthCount={thisMonthCount}
            lastMonthCount={lastMonthCount}
            maxValue={maxValue}
            thisMonth={thisMonthItem}
            lastMonth={lastMonthItem}
          />
        </div>
      </div>
    </div>
  );
};

export default ReportLineGraph;
