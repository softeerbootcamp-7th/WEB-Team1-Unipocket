import { motion } from 'framer-motion';

import {
  type PeriodData,
  TOTAL_ANIMATION_DURATION,
} from '@/components/chart/chartType';

import { cn } from '@/lib/utils';

interface PeriodLineChartProps {
  data: PeriodData[];
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
 * 월별 지출 차트 — 꺾은선 차트
 * 6개의 점을 이은 라인 그래프, 아래에 라벨 표시
 */
const PeriodLineChart = ({
  data,
  className,
  lineColor = 'var(--color-primary-normal)',
  dotColor = 'var(--color-primary-normal)',
  animate = true,
}: PeriodLineChartProps) => {
  const maxValue = Math.max(...data.map((d) => d.value), 1);
  const drawWidth = CHART_WIDTH - PADDING_X * 2;
  const drawHeight = CHART_HEIGHT - PADDING_TOP - PADDING_BOTTOM;

  // 각 데이터 포인트의 (x, y) 좌표 계산
  const points = data.map((item, idx) => {
    const xRatio = data.length > 1 ? idx / (data.length - 1) : 0.5;
    const x = PADDING_X + xRatio * drawWidth;
    const ratio = item.value / maxValue;
    // y축: 값이 클수록 위로 (y 좌표가 작아짐)
    const y = PADDING_TOP + drawHeight * (1 - ratio);
    return { x, y, item };
  });

  // SVG polyline용 좌표 문자열
  const polylinePoints = points.map((p) => `${p.x},${p.y}`).join(' ');

  return (
    <div className={cn('flex w-full flex-col gap-2', className)}>
      <svg
        viewBox={`0 0 ${CHART_WIDTH} ${CHART_HEIGHT}`}
        className="w-full"
        preserveAspectRatio="xMidYMid meet"
      >
        {/* 그리드 라인 (각 데이터 포인트 위치에 세로선) */}
        {points.map((p) => (
          <line
            key={`grid-${p.item.label}`}
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
            key={p.item.label}
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
                      (data.length > 1 ? idx / (data.length - 1) : 0) *
                      TOTAL_ANIMATION_DURATION,
                    duration: 0.2,
                    ease: 'easeOut',
                  }
                : { duration: 0 }
            }
          />
        ))}
      </svg>

      {/* X축 라벨 */}
      <div className="flex w-full justify-between">
        {data.map((item) => (
          <span
            key={item.label}
            className="caption2-medium text-label-alternative w-3.5 text-center whitespace-nowrap"
          >
            {item.label}
          </span>
        ))}
      </div>
    </div>
  );
};

export default PeriodLineChart;
