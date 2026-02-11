import { useId } from 'react';
import { motion } from 'framer-motion';

import {
  type PeriodData,
  TOTAL_ANIMATION_DURATION,
} from '@/components/chart/chartType';
import CurrencyAmountDisplay from '@/components/currency/CurrencyAmountDisplay';

import type { CountryCode } from '@/data/countryCode';
import { cn } from '@/lib/utils';

interface HorizontalBarChartProps {
  data: PeriodData[];
  className?: string;
  barColor?: string;
  bgColor?: string;
  animate?: boolean;
  countryCode?: CountryCode;
}

const BAR_HEIGHT = 12;
const PADDING_RIGHT = 16;
const MAX_BAR_WIDTH = 105;
const DRAW_WIDTH = MAX_BAR_WIDTH - PADDING_RIGHT;
const RADIUS = 0;

/**
 * 주별 지출 차트 — 가로 막대 차트
 * 왼쪽에 라벨, 오른쪽에 가로 막대가 배치됨
 */
const HorizontalBarChart = ({
  data,
  className,
  barColor = 'var(--color-primary-normal)',
  bgColor = 'var(--color-fill-strong)',
  animate = true,
  countryCode = 'KR',
}: HorizontalBarChartProps) => {
  const clipId = useId();
  const maxValue = Math.max(...data.map((d) => d.value), 1);

  return (
    <div className="flex flex-col items-center gap-9.5">
      <div className={cn('flex w-full flex-col gap-3', className)}>
        {data.map((item, idx) => {
          const ratio = item.value / maxValue;
          const barWidth = Math.round(ratio * DRAW_WIDTH);

          return (
            <div key={item.label} className="flex items-center gap-4">
              {/* 라벨 영역 */}
              <div className="flex shrink-0 flex-col">
                <span className="caption2-medium text-label-normal whitespace-nowrap">
                  {item.label}
                </span>
              </div>

              <div className="flex items-center gap-2">
                {/* 막대 영역 */}
                <div className="relative" style={{ width: MAX_BAR_WIDTH }}>
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
                          <clipPath id={`${clipId}-${idx}`}>
                            <motion.rect
                              y={0}
                              height={BAR_HEIGHT}
                              initial={
                                animate
                                  ? { x: 0, width: 0 }
                                  : { x: 0, width: barWidth }
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
                        <g clipPath={`url(#${clipId}-${idx})`}>
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

                {/* 금액 영역 */}
                <CurrencyAmountDisplay
                  amount={item.value}
                  countryCode={countryCode}
                  size="xs"
                />
              </div>
            </div>
          );
        })}
      </div>
      <p className="caption2-medium text-label-assistive">
        *한 주차는 월요일부터 일요일까지 산정됩니다
      </p>
    </div>
  );
};

export default HorizontalBarChart;
