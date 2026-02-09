import { useId } from 'react';
import { motion } from 'framer-motion';

import { cn } from '@/lib/utils';

import { EXPENSE_CHART_COLORS, TOTAL_ANIMATION_DURATION } from '../chartType';

export interface BarData {
  percent: number;
  label?: string;
  currencySignAndName?: string;
}

/**
 * 선택적으로 모서리가 둥근 사각형의 SVG path를 생성
 */
const roundedRectPath = (
  x: number,
  y: number,
  w: number,
  h: number,
  rt: number, // top radius
  rb: number, // bottom radius
) => `
  M ${x + rt},${y}
  h ${w - 2 * rt}
  a ${rt},${rt} 0 0 1 ${rt},${rt}
  v ${h - rt - rb}
  a ${rb},${rb} 0 0 1 -${rb},${rb}
  h -${w - 2 * rb}
  a ${rb},${rb} 0 0 1 -${rb},-${rb}
  v -${h - rt - rb}
  a ${rt},${rt} 0 0 1 ${rt},-${rt}
  z
`;

interface VerticalBarChartProps {
  data: BarData[];
  width?: number;
  height?: number;
  gap?: number;
  className?: string;
  colors?: string[];
  animate?: boolean;
}

const VerticalBarChart = ({
  data,
  width = 30,
  height = 180,
  gap = 0,
  className,
  colors = EXPENSE_CHART_COLORS,
  animate = true,
}: VerticalBarChartProps) => {
  const clipId = useId();
  const totalPercent = data.reduce((sum, item) => sum + item.percent, 0);
  const totalGap = (data.length - 1) * gap;
  const availableHeight = height - totalGap;

  // 각 막대(세그먼트)가 그려질 위치(y)와 높이(height) 계산
  // reduce를 사용하여 이전 막대의 위치를 기반으로 다음 막대의 위치를 쌓아올림
  const segments = data.reduce<
    { y: number; height: number; item: BarData; index: number }[]
  >((acc, item, index) => {
    // 현재 세그먼트의 높이 비율 계산
    const segmentHeight =
      totalPercent > 0 ? (item.percent / totalPercent) * availableHeight : 0;

    // y좌표 계산: 첫 번째 요소면 0, 아니면 이전 요소의 끝(y + height) + 간격(gap)
    const y =
      acc.length > 0
        ? acc[acc.length - 1].y + acc[acc.length - 1].height + gap
        : 0;

    acc.push({ y, height: segmentHeight, item, index });
    return acc;
  }, []);

  return (
    <div
      className={cn('relative flex items-center justify-center', className)}
      style={{ width, height }}
    >
      <svg width={width} height={height} viewBox={`0 0 ${width} ${height}`}>
        <defs>
          {/* 보여질 영역을 정하는 clipPath (animation) */}
          <clipPath id={clipId}>
            <motion.rect
              x={0}
              width={width}
              initial={
                animate ? { y: height, height: 0 } : { y: 0, height: height }
              } // 시작: 바닥에서 높이 0
              animate={{ y: 0, height: height }} // 끝: 전체를 덮음
              transition={
                animate
                  ? { duration: TOTAL_ANIMATION_DURATION, ease: 'easeOut' }
                  : { duration: 0 } // 애니메이션 시간 0
              }
            />
          </clipPath>
        </defs>

        {/* 
          - g 태그로 묶어서 위에서 정의한 clipPath를 적용
          - 막대들은 이미 다 그려져 있지만, clipPath(창문)가 열리는 만큼만 보이는 효과 제공
        */}
        <g clipPath={`url(#${clipId})`}>
          {segments.map(({ y, height: segmentHeight, index }) => {
            const isFirst = index === 0;
            const isLast = index === data.length - 1;

            const radius = 2;
            const rt = isFirst ? radius : 0;
            const rb = isLast ? radius : 0;

            return (
              <path
                key={index}
                // 미리 정의해둔 곡선 그리기 함수 사용
                d={roundedRectPath(0, y, width, segmentHeight, rt, rb)}
                fill={colors[index % colors.length]}
              />
            );
          })}
        </g>
      </svg>
    </div>
  );
};

export default VerticalBarChart;
