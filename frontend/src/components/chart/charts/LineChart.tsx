import { motion } from 'framer-motion';

import { TOTAL_ANIMATION_DURATION } from '@/components/chart/chartType';

import { cn } from '@/lib/utils';

export interface LineChartProps {
  /** 각 점의 값 배열 */
  values: number[];
  className?: string;
  lineColor?: string;
  dotColor?: string;
  animate?: boolean;
}

const CHART_WIDTH = 220;
const GRID_LINE_HEIGHT = 164;
const CHART_HEIGHT = GRID_LINE_HEIGHT;
const PADDING_X = 6;
const PADDING_TOP = 16;
const PADDING_BOTTOM = 8;
const DOT_RADIUS = 4;

/**
 * 꺾은선 차트
 * 점을 이은 라인 그래프만 렌더링
 */
const LineChart = ({
  values,
  className,
  lineColor = 'var(--color-primary-normal)',
  dotColor = 'var(--color-primary-normal)',
  animate = true,
}: LineChartProps) => {
  const maxValue = Math.max(...values, 1);
  const drawWidth = CHART_WIDTH - PADDING_X * 2;
  const drawHeight = CHART_HEIGHT - PADDING_TOP - PADDING_BOTTOM;

  // 각 데이터 포인트의 (x, y) 좌표 계산
  const points = values.map((value, idx) => {
    const xRatio = values.length > 1 ? idx / (values.length - 1) : 0.5;
    const x = PADDING_X + xRatio * drawWidth;
    const ratio = value / maxValue;
    // y축: 값이 클수록 위로 (y 좌표가 작아짐)
    const y = PADDING_TOP + drawHeight * (1 - ratio);
    return { x, y, value };
  });

  // SVG polyline용 좌표 문자열
  const polylinePoints = points.map((p) => `${p.x},${p.y}`).join(' ');

  return (
    <svg
      viewBox={`0 0 ${CHART_WIDTH} ${CHART_HEIGHT}`}
      className={cn('w-full', className)}
      preserveAspectRatio="xMidYMid meet"
    >
      {/* 그리드 라인 (각 데이터 포인트 위치에 세로선) */}
      {points.map((p, idx) => (
        <line
          key={`grid-${idx}`}
          x1={p.x}
          y1={0}
          x2={p.x}
          y2={GRID_LINE_HEIGHT}
          stroke="var(--color-fill-normal)"
          strokeWidth={1}
        />
      ))}

      {/* 하단 가로선 */}
      <line
        x1={points[0]?.x ?? PADDING_X}
        y1={GRID_LINE_HEIGHT}
        x2={points[points.length - 1]?.x ?? CHART_WIDTH - PADDING_X}
        y2={GRID_LINE_HEIGHT}
        stroke="var(--color-fill-normal)"
        strokeWidth={1}
      />

      {/* 꺾은선 */}
      <motion.polyline
        points={polylinePoints}
        fill="none"
        stroke={lineColor}
        strokeWidth={2}
        strokeLinecap="round"
        strokeLinejoin="round"
        initial={animate ? { pathLength: 0, opacity: 0 } : {}}
        animate={{ pathLength: 1, opacity: 1 }}
        transition={
          animate
            ? { duration: TOTAL_ANIMATION_DURATION, ease: 'easeOut' }
            : { duration: 0 }
        }
      />

      {/* 점 */}
      {points.map((p, idx) => (
        <motion.circle
          key={idx}
          cx={p.x}
          cy={p.y}
          r={DOT_RADIUS}
          fill={dotColor}
          initial={animate ? { scale: 0, opacity: 0 } : {}}
          animate={{ scale: 1, opacity: 1 }}
          transition={
            animate
              ? {
                  delay:
                    (values.length > 1 ? idx / (values.length - 1) : 0) *
                    TOTAL_ANIMATION_DURATION,
                  duration: 0.2,
                  ease: 'easeOut',
                }
              : { duration: 0 }
          }
        />
      ))}
    </svg>
  );
};

export default LineChart;
