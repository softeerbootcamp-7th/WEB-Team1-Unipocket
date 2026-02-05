import type { CategoryType } from '@/types/category';
import type { CurrencyType } from '@/types/currency';

import { CATEGORY_COLORS } from '../chartColor';

interface CategoryPieChartProps {
  data: {
    percentage: number;
    categoryName: CategoryType;
    currency?: CurrencyType;
    color?: string;
  }[];
  totalAmount: React.ReactNode;
  size?: number;
  colors?: string[];
}

const CategoryPieChart = ({
  data,
  totalAmount,
  size = 192,
  colors = CATEGORY_COLORS,
}: CategoryPieChartProps) => {
  const center = size / 2;
  const strokeWidth = 10;
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;
  const gap = 4;

  const chartData = data.reduce<
    ((typeof data)[number] & { color: string; startPercentage: number })[]
  >((acc, item, index) => {
    const startPercentage =
      acc.length > 0
        ? acc[acc.length - 1].startPercentage + acc[acc.length - 1].percentage
        : 0;

    acc.push({
      ...item,
      color: item.color || colors[index % colors.length],
      startPercentage,
    });

    return acc;
  }, []);

  return (
    <div
      className="relative flex items-center justify-center"
      style={{ width: size, height: size }}
    >
      <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`}>
        {chartData.map((item, index) => {
          const { percentage, startPercentage, color } = item;
          if (percentage <= 0) return null;

          const arcLength = (circumference * percentage) / 100;
          const drawLength = Math.max(0, arcLength - gap);

          // 전체 원 중에서 startPercentage 지점부터 시작
          const rotation = (startPercentage / 100) * 360 - 90;

          return (
            <circle
              key={index}
              cx={center}
              cy={center}
              r={radius}
              fill="transparent"
              stroke={color}
              strokeWidth={strokeWidth}
              strokeDasharray={`${drawLength} ${circumference - drawLength}`} // 그릴 길이, 빈 길이
              strokeDashoffset={0}
              transform={`rotate(${rotation} ${center} ${center})`}
              strokeLinecap="butt"
            />
          );
        })}
      </svg>
      <div className="absolute text-center">
        <span className="figure-heading1-semibold text-label-normal">
          {totalAmount}
        </span>
      </div>
    </div>
  );
};

export default CategoryPieChart;
