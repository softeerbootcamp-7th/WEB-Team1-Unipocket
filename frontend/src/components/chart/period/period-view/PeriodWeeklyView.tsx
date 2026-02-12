import HorizontalBar from '@/components/chart/charts/HorizontalBar';
import {
  PERIOD_SKELETON_COLOR,
  type PeriodData,
} from '@/components/chart/chartType';
import CurrencyAmountDisplay from '@/components/currency/CurrencyAmountDisplay';

import type { CountryCode } from '@/data/countryCode';

interface PeriodWeeklyViewProps {
  data: PeriodData[];
  countryCode?: CountryCode;
  animate?: boolean;
  isLoading?: boolean;
}

/**
 * 주별 지출 뷰 — 가로 막대 차트 + 라벨 + 금액 + 안내문구
 */
const PeriodWeeklyView = ({
  data,
  countryCode = 'KR',
  animate = true,
  isLoading = false,
}: PeriodWeeklyViewProps) => {
  const maxValue = Math.max(1, ...data.map((d) => d.value));

  const barColor = isLoading ? PERIOD_SKELETON_COLOR : undefined;

  return (
    <div className="flex flex-col items-center gap-9.5">
      <div className="flex w-full flex-col gap-3">
        {data.map((item) => (
          <div key={item.label} className="flex items-center gap-4">
            {/* 라벨 영역 */}
            <div className="flex shrink-0 flex-col">
              {isLoading ? (
                <div className="bg-fill-normal h-3 w-8 animate-pulse rounded" />
              ) : (
                <span className="caption2-medium text-label-normal whitespace-nowrap">
                  {item.label}
                </span>
              )}
            </div>

            <div className="flex items-center gap-2">
              {/* 단일 가로 막대 */}
              <HorizontalBar
                value={item.value}
                maxValue={maxValue}
                barColor={barColor}
                animate={!isLoading && animate}
              />

              {/* 금액 영역 */}
              {isLoading ? (
                <div className="bg-fill-normal h-4 w-14.5 animate-pulse rounded" />
              ) : (
                <CurrencyAmountDisplay
                  amount={item.value}
                  countryCode={countryCode}
                  size="xs"
                />
              )}
            </div>
          </div>
        ))}
      </div>
      <p className="caption2-medium text-label-assistive">
        *한 주차는 월요일부터 일요일까지 산정됩니다
      </p>
    </div>
  );
};

export default PeriodWeeklyView;
