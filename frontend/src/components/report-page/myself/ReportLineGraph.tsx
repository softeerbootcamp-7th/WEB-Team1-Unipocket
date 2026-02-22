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
  const maxDay = isCurrentMonth
    ? lastMonth.dayCount
    : Math.min(thisMonth.dayCount, lastMonth.dayCount);

  const MIN_GAP = 10; // 라벨 간 최소 간격 (%)

  const todayPosition =
    isCurrentMonth && maxDay > 1
      ? ((thisMonth.dayCount - 1) / (maxDay - 1)) * 100
      : null;

  // 1일이거나 마지막 날과 같으면 중복 제거, 너무 가까우면 최소 간격 보장
  const showTodayLabel =
    todayPosition !== null &&
    todayPosition > MIN_GAP &&
    todayPosition < 100 - MIN_GAP;

  const basePositions = [0, 100];
  const baseLabels = ['1일', `${maxDay}일`];

  let positions = basePositions;
  let labels = baseLabels;

  if (isCurrentMonth && showTodayLabel && todayPosition !== null) {
    positions = [0, todayPosition, 100];
    labels = ['1일', `${thisMonth.dayCount}일`, `${maxDay}일`];
  }

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
            maxDay={maxDay}
            maxValue={maxValue}
          />
        </div>
      </div>
    </div>
  );
};

export default ReportLineGraph;
