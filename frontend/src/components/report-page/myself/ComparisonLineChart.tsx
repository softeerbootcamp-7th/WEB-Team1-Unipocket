import {
  buildLinePath,
  type ChartItem,
} from '@/components/report-page/myself/buildPath';

interface ComparisonLineChartProps {
  thisMonth: ChartItem[];
  prevMonth: ChartItem[];
  width?: number;
  height?: number;
}

const ComparisonLineChart = ({
  thisMonth,
  prevMonth,
  width = 320,
  height = 180,
}: ComparisonLineChartProps) => {
  const allValues = [
    ...thisMonth.map((v) => Number(v.cumulatedAmount)),
    ...prevMonth.map((v) => Number(v.cumulatedAmount)),
  ];

  const maxValue = Math.max(...allValues);

  const thisPath = buildLinePath(thisMonth, width, height, maxValue);
  const prevPath = buildLinePath(prevMonth, width, height, maxValue);

  return (
    <svg width={width} height={height}>
      <path d={prevPath} fill="none" stroke="#C4C7CC" strokeWidth={3} />
      <path d={thisPath} fill="none" stroke="#2B8C94" strokeWidth={3} />
    </svg>
  );
};

export default ComparisonLineChart;
