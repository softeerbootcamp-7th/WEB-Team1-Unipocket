import { motion } from 'framer-motion';

import { useAutoFitScale } from '@/hooks/useAutoFitScale';

import type { CategoryType } from '@/types/category';
import type { CurrencyType } from '@/types/currency';

import { CATEGORY_CHART_COLORS } from '../chartType';

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
  colors = CATEGORY_CHART_COLORS,
}: CategoryPieChartProps) => {
  const center = size / 2;
  const strokeWidth = 10;
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;
  const gap = 4;
  const TOTAL_DURATION = 0.8;

  const MAX_WIDTH = 130; // w-32.5 (130px)
  const { ref: contentRef, scale } = useAutoFitScale<HTMLDivElement>(
    MAX_WIDTH,
    [totalAmount],
  );

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

          // 이 세그먼트의 시작 시간 (딜레이)
          const delay = (startPercentage / 100) * TOTAL_DURATION;
          // 이 세그먼트가 채워지는 데 걸리는 시간
          const duration = (percentage / 100) * TOTAL_DURATION;

          return (
            <motion.circle
              key={index}
              cx={center}
              cy={center}
              r={radius}
              fill="transparent"
              stroke={color}
              strokeWidth={strokeWidth}
              strokeDashoffset={0}
              transform={`rotate(${rotation} ${center} ${center})`}
              strokeLinecap="butt"
              initial={{ strokeDasharray: `0 ${circumference}` }}
              animate={{
                strokeDasharray: `${drawLength} ${circumference - drawLength}`,
              }}
              transition={{
                duration,
                ease: 'linear',
                delay,
              }}
            />
          );
        })}
      </svg>
      <motion.div
        className="absolute flex w-32.5 justify-center text-center"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ duration: TOTAL_DURATION }}
      >
        <div
          ref={contentRef}
          className="whitespace-nowrap"
          style={{
            transform: `scale(${scale})`,
            transformOrigin: 'center',
          }}
        >
          {totalAmount}
        </div>
      </motion.div>
    </div>
  );
};

export default CategoryPieChart;
