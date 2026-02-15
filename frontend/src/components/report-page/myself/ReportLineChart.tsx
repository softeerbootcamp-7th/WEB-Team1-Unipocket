import {
  buildAreaPath,
  buildLinePath,
} from '@/components/report-page/myself/buildPath';
import { type ChartItem } from '@/components/report-page/reportType';

type MonthlyChartData = {
  dayCount: number;
  items: ChartItem[];
};

interface ReportLineChartProps {
  thisMonth: MonthlyChartData;
  lastMonth: MonthlyChartData;
  maxValue: number;
  width?: number;
  height?: number;
}

const ReportLineChart = ({
  thisMonth,
  lastMonth,
  maxValue,
  width = 348,
  height = 140,
}: ReportLineChartProps) => {
  const maxDay = Math.max(thisMonth.dayCount, lastMonth.dayCount);

  const thisPath = buildLinePath(
    thisMonth.items,
    width,
    height,
    maxValue,
    maxDay,
  );
  const lastLinePath = buildLinePath(
    lastMonth.items,
    width,
    height,
    maxValue,
    maxDay,
  );
  const lastAreaPath = buildAreaPath(
    lastMonth.items,
    width,
    height,
    maxValue,
    maxDay,
  );

  const thisMonthWidth = (thisMonth.items.length / maxDay) * width;

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
