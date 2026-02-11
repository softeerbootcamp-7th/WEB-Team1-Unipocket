import { motion } from 'framer-motion';

import { TOTAL_ANIMATION_DURATION } from '@/components/chart/chartType';

import { cn } from '@/lib/utils';

export interface VerticalBarProps {
  /** 현재 막대의 값 */
  value: number;
  /** 최대값 (비율 계산용) */
  maxValue: number;
  className?: string;
  barColor?: string;
  bgColor?: string;
  animate?: boolean;
}

const BAR_WIDTH = 15;
const CHART_HEIGHT = 145;
const PADDING_TOP = 16;
const DRAW_HEIGHT = CHART_HEIGHT - PADDING_TOP;
const RADIUS = 0;

/**
 * 단일 세로 막대
 * 배경 바가 깔려 있고, 그 위에 실제 값 비율 막대가 올라옴
 */
const VerticalBar = ({
  value,
  maxValue,
  className,
  barColor = 'var(--color-primary-normal)',
  bgColor = 'var(--color-fill-disable)',
  animate = true,
}: VerticalBarProps) => {
  const safeMaxValue = Math.max(1, maxValue);
  const ratio = value / safeMaxValue;
  const barHeight = Math.round(ratio * DRAW_HEIGHT);

  return (
    <div
      className={cn('relative flex items-end justify-center', className)}
      style={{ width: BAR_WIDTH, height: CHART_HEIGHT }}
    >
      {/* 배경 바 (항상 full height) */}
      <svg width={BAR_WIDTH} height={CHART_HEIGHT} className="absolute inset-0">
        <rect
          x={0}
          y={0}
          width={BAR_WIDTH}
          height={CHART_HEIGHT}
          rx={RADIUS}
          ry={RADIUS}
          fill={bgColor}
        />
      </svg>

      {/* 실제 값 바 */}
      {barHeight > 0 && (
        <svg
          width={BAR_WIDTH}
          height={CHART_HEIGHT}
          className="absolute inset-0"
        >
          <motion.rect
            x={0}
            width={BAR_WIDTH}
            rx={RADIUS}
            ry={RADIUS}
            fill={barColor}
            initial={
              animate
                ? { y: CHART_HEIGHT, height: 0 }
                : { y: CHART_HEIGHT - barHeight, height: barHeight }
            }
            animate={{ y: CHART_HEIGHT - barHeight, height: barHeight }}
            transition={
              animate
                ? {
                    duration: TOTAL_ANIMATION_DURATION,
                    ease: 'easeOut',
                  }
                : { duration: 0 }
            }
          />
        </svg>
      )}
    </div>
  );
};

export default VerticalBar;
