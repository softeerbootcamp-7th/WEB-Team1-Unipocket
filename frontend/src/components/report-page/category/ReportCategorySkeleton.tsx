import ReportContainer from '@/components/report-page/layout/ReportContainer';
import ReportContent from '@/components/report-page/layout/ReportContent';
import ReportLegend from '@/components/report-page/ReportLegend';
import VerticalGrid from '@/components/report-page/VerticalGrid';

import { CATEGORIES, type CategoryId } from '@/types/category';

import { getCountryInfo } from '@/lib/country';
import { useRequiredAccountBook } from '@/stores/accountBookStore';

const SKELETON_STEPS = 6;
const SKELETON_MAX = 150;
const SKELETON_LABELS = Array.from(
  { length: SKELETON_STEPS + 1 },
  (_, i) => (SKELETON_MAX / SKELETON_STEPS) * i,
);

const SKELETON_ME_WIDTH = 45;
const SKELETON_OTHER_WIDTH = 35;

const SKELETON_CATEGORY_IDS = (
  Object.keys(CATEGORIES) as unknown as CategoryId[]
).filter((id) => Number(id) !== 0 && Number(id) !== 9);

interface ReportCategorySkeletonProps {
  reason: 'me' | 'other';
}

const ReportCategorySkeleton = ({ reason }: ReportCategorySkeletonProps) => {
  const { localCountryCode } = useRequiredAccountBook();
  const countryName = getCountryInfo(localCountryCode)?.countryName ?? '';

  return (
    <ReportContainer title="월별 지출 비교">
      <ReportContent className="h-fit w-full">
        <div className="flex flex-col gap-1">
          <h3 className="headline1-bold text-label-neutral">
            {reason === 'me'
              ? '나의 데이터가 아직 충분하지 않아요'
              : `${countryName} 교환학생의 데이터가 아직 충분하지 않아요`}
          </h3>
          <p className="body2-normal-medium text-label-alternative">
            데이터가 더 쌓이면 하단처럼 비교 통계를 제공할게요
          </p>
        </div>

        <div className="flex w-full flex-col gap-1">
          <div className="flex justify-end gap-4">
            <ReportLegend label="나" color="skeletonPrimary" />
            <ReportLegend label="다른 학생" color="skeletonSecondary" />
          </div>

          <div className="relative h-125.25 pt-4.75">
            <VerticalGrid
              steps={SKELETON_STEPS}
              labels={SKELETON_LABELS}
              className="left-15"
            />
            <div className="relative z-10 flex flex-col gap-4.5">
              {SKELETON_CATEGORY_IDS.map((id) => (
                <div key={id} className="flex h-10 items-center gap-4">
                  <span className="label1-normal-medium text-label-neutral flex w-12 justify-end">
                    {CATEGORIES[id].name}
                  </span>

                  <div className="flex w-full flex-col justify-between">
                    <div className="flex flex-1 items-center gap-2">
                      <div
                        className="bg-cool-neutral-80 h-3 origin-left rounded-r-xs"
                        style={{ width: `${SKELETON_ME_WIDTH}%` }}
                      />
                      <span className="body2-normal-bold text-cool-neutral-80">
                        ₩ ???
                      </span>
                    </div>

                    <div className="flex flex-1 items-center gap-2">
                      <div
                        className="bg-cool-neutral-95 h-3 origin-left rounded-r-xs"
                        style={{ width: `${SKELETON_OTHER_WIDTH}%` }}
                      />
                      <span className="label2-medium text-cool-neutral-80">
                        ₩ ???
                      </span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </ReportContent>
    </ReportContainer>
  );
};

export default ReportCategorySkeleton;
