import {
  buildAreaPath,
  buildLinePath,
} from '@/components/report-page/myself/buildPath';

import { type AnalysisChartItem } from '@/api/account-books/type';

type MonthlyChartData = {
  dayCount: number;
  items: AnalysisChartItem[];
};

interface ReportLineChartProps {
  thisMonth: MonthlyChartData;
  lastMonth: MonthlyChartData;
  maxValue: number;
  width?: number;
  height?: number;
  isCurrentMonth: boolean;
}

const ReportLineChart = ({
  thisMonth,
  lastMonth,
  maxValue,
  width = 352,
  height = 140,
  isCurrentMonth,
}: ReportLineChartProps) => {
  const maxDay = isCurrentMonth
    ? lastMonth.dayCount
    : Math.min(thisMonth.dayCount, lastMonth.dayCount);
  const thisItems = thisMonth.items.slice(0, maxDay);
  const lastItems = lastMonth.items.slice(0, maxDay);

  const thisPath = buildLinePath(thisItems, width, height, maxValue, maxDay);
  const lastLinePath = buildLinePath(
    lastItems,
    width,
    height,
    maxValue,
    maxDay,
  );
  const lastAreaPath = buildAreaPath(
    lastItems,
    width,
    height,
    maxValue,
    maxDay,
  );

  const thisMonthWidth = (thisItems.length / maxDay) * width;

  return (
    <svg width={width} height={height}>
      <defs>
        <clipPath id="thisMonthClip">
          <rect x="0" y="0" width={thisMonthWidth} height={height} />
        </clipPath>
        <linearGradient id="graphGradient" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stopColor="rgba(194, 196, 200, 0.50)" />
          <stop offset="100%" stopColor="rgba(255, 255, 255, 0.50)" />
        </linearGradient>
      </defs>
      <path d={lastAreaPath} fill="url(#graphGradient)" stroke="none" />
      <path d={lastLinePath} fill="none" stroke="#C2C4C8" strokeWidth={2.5} />
      <path
        d={thisPath}
        fill="none"
        stroke="#44A3B6"
        strokeWidth={2.5}
        strokeDasharray="1000"
        className="animate-draw-path"
        clipPath="url(#thisMonthClip)"
      />
    </svg>
  );
};

export default ReportLineChart;
