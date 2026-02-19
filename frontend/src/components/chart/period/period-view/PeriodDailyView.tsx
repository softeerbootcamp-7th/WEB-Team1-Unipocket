import VerticalBar from '@/components/chart/charts/VerticalBar';
import {
  PERIOD_SKELETON_COLOR,
  type PeriodData,
} from '@/components/chart/chartType';

interface PeriodDailyViewProps {
  data: PeriodData[];
  animate?: boolean;
  isPreview?: boolean;
}

/**
 * 일별 지출 뷰 — 세로 막대 차트 + 라벨
 */
const PeriodDailyView = ({
  data,
  animate = true,
  isPreview = false,
}: PeriodDailyViewProps) => {
  const maxValue = Math.max(1, ...data.map((d) => d.value));

  const barColor = isPreview ? PERIOD_SKELETON_COLOR : undefined;

  return (
    <div className="flex w-full flex-col gap-2.5">
      {/* 차트 영역 */}
      <div className="flex w-full items-end justify-between">
        {data.map((item) => (
          <div key={item.label} className="flex flex-1 justify-center">
            <VerticalBar
              value={item.value}
              maxValue={maxValue}
              barColor={barColor}
              animate={!isPreview && animate}
            />
          </div>
        ))}
      </div>

      {/* 라벨 */}
      <div className="flex w-full justify-between">
        {data.map((item) =>
          isPreview ? (
            <div key={item.label} className="flex flex-1 justify-center">
              <div className="bg-fill-normal h-3 w-4 animate-pulse rounded" />
            </div>
          ) : (
            <span
              key={item.label}
              className="figure-caption1-medium text-label-alternative flex-1 text-center whitespace-nowrap"
            >
              {item.label}
            </span>
          ),
        )}
      </div>
    </div>
  );
};

export default PeriodDailyView;
