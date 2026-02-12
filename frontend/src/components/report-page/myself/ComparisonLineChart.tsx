interface ChartItem {
  date: string;
  cumulatedAmount: string;
}

interface ComparisonLineChartProps {
  thisMonth: ChartItem[];
  prevMonth: ChartItem[];
  width?: number;
  height?: number;
}

const buildLinePath = (
  data: ChartItem[],
  width: number,
  height: number,
  maxValue: number,
) => {
  const stepX = width / (data.length - 1);

  return data
    .map((item, index) => {
      const x = stepX * index;
      const value = Number(item.cumulatedAmount);
      const y = height - (value / maxValue) * height;

      return `${index === 0 ? 'M' : 'L'} ${x} ${y}`;
    })
    .join(' ');
};

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
      <path d={prevPath} fill="none" stroke="#C2C4C8" strokeWidth={3} />
      <path d={thisPath} fill="none" stroke="#44A3B6" strokeWidth={3} />
    </svg>
  );
};

export default ComparisonLineChart;
