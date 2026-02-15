import ReportLineChart from '@/components/report-page/myself/ReportLineChart';
import ReportLegend from '@/components/report-page/ReportLegend';
import { type ChartItem } from '@/components/report-page/reportType';
import VerticalGrid from '@/components/report-page/VerticalGrid';

type MonthlyGraphData = {
  label: string;
  dayCount: number;
  items: ChartItem[];
};

interface ReportLineGraphProps {
  thisMonth: MonthlyGraphData;
  lastMonth: MonthlyGraphData;
  maxValue: number;
}

const ReportLineGraph = ({
  thisMonth,
  lastMonth,
  maxValue,
}: ReportLineGraphProps) => {
  const maxDays = Math.max(thisMonth.dayCount, lastMonth.dayCount);

  const positions = [0, ((thisMonth.dayCount - 1) / (maxDays - 1)) * 100, 100];
  const labels = ['1일', `${thisMonth.dayCount}일`, `${maxDays}일`];

  return (
    <div className="flex flex-col gap-4">
      <div className="flex justify-end gap-4">
        <ReportLegend label={thisMonth.label} color="primary" variant="line" />
        <ReportLegend
          label={lastMonth.label}
          color="secondary"
          variant="line"
        />
      </div>
      <div className="relative h-42 pl-3">
        <VerticalGrid positions={positions} labels={labels} className="pl-3" />
        <div className="relative z-10 pt-2">
          <ReportLineChart
            thisMonth={thisMonth}
            lastMonth={lastMonth}
            maxValue={maxValue}
          />
        </div>
      </div>
    </div>
  );
};

export default ReportLineGraph;
