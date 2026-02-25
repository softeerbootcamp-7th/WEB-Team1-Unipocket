import ReportContainer from '@/components/report-page/layout/ReportContainer';
import ReportContent from '@/components/report-page/layout/ReportContent';
import {
  buildAreaPath,
  buildLinePath,
} from '@/components/report-page/myself/buildPath';
import ReportLegend from '@/components/report-page/ReportLegend';
import VerticalGrid from '@/components/report-page/VerticalGrid';

const WIDTH = 352;
const HEIGHT = 140;
const MAX_VALUE = 100;

// 더미 이번 달 누적 지출 데이터 (15일까지, date 필드는 buildPath에서 사용되지 않음)
// 16개 항목(index 0~15) → 마지막 x = 15/30*100 = 50% (TODAY_POSITION과 일치)
const THIS_MONTH_ITEMS = [
  { date: '', cumulatedAmount: '0' },
  { date: '', cumulatedAmount: '1' },
  { date: '', cumulatedAmount: '3' },
  { date: '', cumulatedAmount: '6' },
  { date: '', cumulatedAmount: '9' },
  { date: '', cumulatedAmount: '12' },
  { date: '', cumulatedAmount: '15' },
  { date: '', cumulatedAmount: '17' },
  { date: '', cumulatedAmount: '19' },
  { date: '', cumulatedAmount: '21' },
  { date: '', cumulatedAmount: '24' },
  { date: '', cumulatedAmount: '26' },
  { date: '', cumulatedAmount: '29' },
  { date: '', cumulatedAmount: '31' },
  { date: '', cumulatedAmount: '32' },
  { date: '', cumulatedAmount: '34' },
];

// 15일의 x 위치: (15-1) / (31-1) * 100 = 14/30*100 ≈ 47% → 시각적 중간값 50%
const TODAY_POSITION = 50;

// 더미 누적 지출 데이터 (지난달 기준, 31일, date 필드는 buildPath에서 사용되지 않음)
const LAST_MONTH_ITEMS = [
  { date: '', cumulatedAmount: '0' },
  { date: '', cumulatedAmount: '0' },
  { date: '', cumulatedAmount: '0' },
  { date: '', cumulatedAmount: '1' },
  { date: '', cumulatedAmount: '2' },
  { date: '', cumulatedAmount: '3' },
  { date: '', cumulatedAmount: '4' },
  { date: '', cumulatedAmount: '5' },
  { date: '', cumulatedAmount: '7' },
  { date: '', cumulatedAmount: '9' },
  { date: '', cumulatedAmount: '11' },
  { date: '', cumulatedAmount: '13' },
  { date: '', cumulatedAmount: '16' },
  { date: '', cumulatedAmount: '19' },
  { date: '', cumulatedAmount: '22' },
  { date: '', cumulatedAmount: '25' },
  { date: '', cumulatedAmount: '28' },
  { date: '', cumulatedAmount: '32' },
  { date: '', cumulatedAmount: '36' },
  { date: '', cumulatedAmount: '40' },
  { date: '', cumulatedAmount: '44' },
  { date: '', cumulatedAmount: '49' },
  { date: '', cumulatedAmount: '54' },
  { date: '', cumulatedAmount: '59' },
  { date: '', cumulatedAmount: '64' },
  { date: '', cumulatedAmount: '69' },
  { date: '', cumulatedAmount: '75' },
  { date: '', cumulatedAmount: '81' },
  { date: '', cumulatedAmount: '87' },
  { date: '', cumulatedAmount: '93' },
  { date: '', cumulatedAmount: '100' },
];

const MAX_DAY = LAST_MONTH_ITEMS.length;

const thisLinePath = buildLinePath(
  THIS_MONTH_ITEMS,
  WIDTH,
  HEIGHT,
  MAX_VALUE,
  MAX_DAY,
);
const lastLinePath = buildLinePath(
  LAST_MONTH_ITEMS,
  WIDTH,
  HEIGHT,
  MAX_VALUE,
  MAX_DAY,
);
const lastAreaPath = buildAreaPath(
  LAST_MONTH_ITEMS,
  WIDTH,
  HEIGHT,
  MAX_VALUE,
  MAX_DAY,
);

const ReportMyselfSkeleton = () => {
  return (
    <ReportContainer title="전월 대비 지출 비교">
      <ReportContent className="h-fit w-109 gap-3 pr-6">
        <div className="flex flex-col gap-1">
          <h3 className="headline1-bold text-label-neutral">
            지출 기록을 시작해보세요!
          </h3>
          <span className="body2-normal-medium text-label-alternative">
            데이터가 더 쌓이면 하단처럼 비교 통계를 제공할게요
          </span>
        </div>

        <div className="flex flex-col gap-4">
          <div className="flex justify-end gap-4">
            <ReportLegend
              label="이번 달"
              color="skeletonPrimary"
              variant="line"
            />
            <ReportLegend
              label="지난 달"
              color="skeletonSecondary"
              variant="line"
            />
          </div>
          <div className="relative h-42 pl-3">
            <VerticalGrid
              positions={[0, TODAY_POSITION, 100]}
              labels={['1일', '15일', `${MAX_DAY}일`]}
              className="pl-3"
            />
            <div className="relative z-10 pt-2">
              <svg width={WIDTH} height={HEIGHT}>
                <defs>
                  <linearGradient
                    id="skeletonGraphGradient"
                    x1="0%"
                    y1="0%"
                    x2="0%"
                    y2="100%"
                  >
                    <stop offset="0%" stopColor="rgba(194, 196, 200, 0.50)" />
                    <stop offset="100%" stopColor="rgba(255, 255, 255, 0.50)" />
                  </linearGradient>
                </defs>
                <path
                  d={lastAreaPath}
                  fill="url(#skeletonGraphGradient)"
                  stroke="none"
                />
                <path
                  d={lastLinePath}
                  fill="none"
                  stroke="#DBDCDF"
                  strokeWidth={2.5}
                />
                <path
                  d={thisLinePath}
                  fill="none"
                  stroke="#AEB0B6"
                  strokeWidth={2.5}
                />
              </svg>
            </div>
          </div>
        </div>
      </ReportContent>
    </ReportContainer>
  );
};

export default ReportMyselfSkeleton;
