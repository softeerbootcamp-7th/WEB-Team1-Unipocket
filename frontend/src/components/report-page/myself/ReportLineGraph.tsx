import ReportLineChart from '@/components/report-page/myself/ReportLineChart';
import ReportLegend from '@/components/report-page/ReportLegend';
import VerticalGrid from '@/components/report-page/VerticalGrid';

import { type AnalysisChartItem } from '@/api/account-books/type';

type MonthlyGraphData = {
  label: string;
  dayCount: number;
  items: AnalysisChartItem[];
};

interface ReportLineGraphProps {
  thisMonth: MonthlyGraphData;
  lastMonth: MonthlyGraphData;
  maxValue: number;
  isCurrentMonth: boolean;
}

const ReportLineGraph = ({
  thisMonth,
  lastMonth,
  maxValue,
  isCurrentMonth,
}: ReportLineGraphProps) => {
  const endDay = isCurrentMonth
    ? lastMonth.dayCount
    : Math.min(thisMonth.dayCount, lastMonth.dayCount);

  const positions = isCurrentMonth
    ? [0, ((thisMonth.dayCount - 1) / (endDay - 1)) * 100, 100]
    : [0, 100];
  const labels = isCurrentMonth
    ? ['1일', `${thisMonth.dayCount}일`, `${endDay}일`]
    : ['1일', `${endDay}일`];

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
            isCurrentMonth={isCurrentMonth}
          />
        </div>
      </div>
    </div>
  );
};

export default ReportLineGraph;
