import LineChart from '@/components/chart/charts/LineChart';
import {
  PERIOD_SKELETON_COLOR,
  type PeriodData,
} from '@/components/chart/chartType';

interface PeriodMonthlyViewProps {
  data: PeriodData[];
  animate?: boolean;
  isLoading?: boolean;
}

/**
 * 월별 지출 뷰 — 꺾은선 차트 + 라벨
 */
const PeriodMonthlyView = ({
  data,
  animate = true,
  isLoading = false,
}: PeriodMonthlyViewProps) => {
  const values = data.map((d) => d.value);

  const dotColor = isLoading ? PERIOD_SKELETON_COLOR : undefined;
  const lineColor = isLoading ? PERIOD_SKELETON_COLOR : undefined;

  return (
    <div className="flex w-full flex-col gap-2">
      {/* 차트 */}
      <LineChart
        values={values}
        lineColor={lineColor}
        dotColor={dotColor}
        animate={!isLoading && animate}
      />

      {/* X축 라벨 */}
      <div className="flex w-full justify-between">
        {data.map((item) =>
          isLoading ? (
            <div
              key={item.label}
              className="bg-fill-normal h-3 w-3.5 animate-pulse rounded"
            />
          ) : (
            <span
              key={item.label}
              className="caption2-medium text-label-alternative w-3.5 text-center whitespace-nowrap"
            >
              {item.label}
            </span>
          ),
        )}
      </div>
    </div>
  );
};

export default PeriodMonthlyView;
