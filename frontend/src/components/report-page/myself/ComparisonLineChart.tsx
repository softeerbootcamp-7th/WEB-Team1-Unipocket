interface ChartItem {
  date: string;
  cumulatedAmount: string;
}

interface ComparisonLineChartProps {
  thisMonth: ChartItem[];
  prevMonth: ChartItem[];
  thisMonthCount: number;
  lastMonthCount: number;
  maxValue: number;
  width?: number;
  height?: number;
}

const buildLinePath = (
  data: ChartItem[],
  width: number,
  height: number,
  maxValue: number,
  maxDay: number,
) => {
  const stepX = width / (maxDay - 1);

  return data
    .map((item, index) => {
      const x = stepX * index;
      const value = Number(item.cumulatedAmount);
      const y = height - (value / maxValue) * height;

      return `${index === 0 ? 'M' : 'L'} ${x} ${y}`;
    })
    .join(' ');
};

const buildAreaPath = (
  data: ChartItem[],
  width: number,
  height: number,
  maxValue: number,
  maxDay: number,
) => {
  const stepX = width / (maxDay - 1);
  const linePath = data
    .map((item, index) => {
      const x = stepX * index;
      const value = Number(item.cumulatedAmount);
      const y = height - (value / maxValue) * height;

      return `${index === 0 ? 'M' : 'L'} ${x} ${y}`;
    })
    .join(' ');

  const lastIndex = data.length - 1;
  const lastX = stepX * lastIndex;
  const closingPath = `L ${lastX} ${height} L 0 ${height} Z`;

  return linePath + ' ' + closingPath;
};

const ComparisonLineChart = ({
  thisMonthCount,
  lastMonthCount,
  thisMonth,
  prevMonth,
  maxValue,
  width = 360,
  height = 140,
}: ComparisonLineChartProps) => {
  const maxDay = Math.max(thisMonthCount, lastMonthCount);

  const thisPath = buildLinePath(thisMonth, width, height, maxValue, maxDay);
  const prevAreaPath = buildAreaPath(
    prevMonth,
    width,
    height,
    maxValue,
    maxDay,
  );

  const thisMonthWidth = (thisMonth.length / maxDay) * width;

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
      <path d={prevAreaPath} fill="url(#graphGradient)" stroke="none" />
      <path
        d={buildLinePath(prevMonth, width, height, maxValue, maxDay)}
        fill="none"
        stroke="#C2C4C8"
        strokeWidth={2.5}
      />
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

export default ComparisonLineChart;
