import { motion } from 'framer-motion';

import { TOTAL_ANIMATION_DURATION } from '@/components/common/chart/chartType';

interface PieChartSegment {
  percentage: number;
  color: string;
}

interface PieChartProps {
  data: PieChartSegment[];
  size?: number;
  children?: React.ReactNode;
  animate?: boolean;
}

const PieChart = ({
  data,
  size = 192,
  children,
  animate = true,
}: PieChartProps) => {
  const strokeWidth = 10;
  const gap = 4;
  const center = size / 2;
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;

  // 각 데이터의 누적 비율(startPercentage) 계산
  const chartData = data.reduce<
    (PieChartSegment & { startPercentage: number })[]
  >((acc, item) => {
    const startPercentage =
      acc.length > 0
        ? acc[acc.length - 1].startPercentage + acc[acc.length - 1].percentage
        : 0;

    acc.push({
      ...item,
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
          const delay = (startPercentage / 100) * TOTAL_ANIMATION_DURATION;
          // 이 세그먼트가 채워지는 데 걸리는 시간
          const duration = (percentage / 100) * TOTAL_ANIMATION_DURATION;

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
              initial={
                animate
                  ? { strokeDasharray: `0 ${circumference}` }
                  : {
                      strokeDasharray: `${drawLength} ${circumference - drawLength}`,
                    }
              }
              animate={{
                strokeDasharray: `${drawLength} ${circumference - drawLength}`,
              }}
              transition={
                animate ? { duration, ease: 'linear', delay } : { duration: 0 }
              }
            />
          );
        })}
      </svg>
      {children && (
        <motion.div
          className="absolute flex w-32.5 justify-center text-center"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: TOTAL_ANIMATION_DURATION }}
        >
          {children}
        </motion.div>
      )}
    </div>
  );
};

export default PieChart;
