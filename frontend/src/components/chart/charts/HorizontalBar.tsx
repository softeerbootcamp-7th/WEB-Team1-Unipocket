import { useId } from 'react';
import { motion } from 'framer-motion';

import { TOTAL_ANIMATION_DURATION } from '@/components/chart/chartType';

import { cn } from '@/lib/utils';

export interface HorizontalBarProps {
  /** 현재 막대의 값 */
  value: number;
  /** 최대값 (비율 계산용) */
  maxValue: number;
  className?: string;
  barColor?: string;
  bgColor?: string;
  animate?: boolean;
}

const BAR_HEIGHT = 12;
const PADDING_RIGHT = 16;
const MAX_BAR_WIDTH = 105;
const DRAW_WIDTH = MAX_BAR_WIDTH - PADDING_RIGHT;
const RADIUS = 0;

/**
 * 단일 가로 막대
 */
const HorizontalBar = ({
  value,
  maxValue,
  className,
  barColor = 'var(--color-primary-normal)',
  bgColor = 'var(--color-fill-disable)',
  animate = true,
}: HorizontalBarProps) => {
  const clipId = useId();
  const safeMaxValue = Math.max(maxValue, 1);
  const ratio = value / safeMaxValue;
  const barWidth = Math.round(ratio * DRAW_WIDTH);

  return (
    <div
      className={cn('relative', className)}
      style={{ width: MAX_BAR_WIDTH, height: BAR_HEIGHT }}
    >
      <svg width="100%" height={BAR_HEIGHT}>
        {/* 배경 바 */}
        <rect
          x={0}
          y={0}
          width="100%"
          height={BAR_HEIGHT}
          rx={RADIUS}
          ry={RADIUS}
          fill={bgColor}
        />

        {/* 실제 값 바 */}
        {barWidth > 0 && (
          <>
            <defs>
              <clipPath id={clipId}>
                <motion.rect
                  y={0}
                  height={BAR_HEIGHT}
                  initial={
                    animate ? { x: 0, width: 0 } : { x: 0, width: barWidth }
                  }
                  animate={{ x: 0, width: barWidth }}
                  transition={
                    animate
                      ? {
                          duration: TOTAL_ANIMATION_DURATION,
                          ease: 'easeOut',
                        }
                      : { duration: 0 }
                  }
                />
              </clipPath>
            </defs>
            <g clipPath={`url(#${clipId})`}>
              <rect
                x={0}
                y={0}
                width={barWidth}
                height={BAR_HEIGHT}
                rx={RADIUS}
                ry={RADIUS}
                fill={barColor}
              />
            </g>
          </>
        )}
      </svg>
    </div>
  );
};

export default HorizontalBar;
