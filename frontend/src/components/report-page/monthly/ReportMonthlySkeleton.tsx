import ReportContainer from '@/components/report-page/layout/ReportContainer';
import ReportContent from '@/components/report-page/layout/ReportContent';

import { getCountryInfo } from '@/lib/country';
import { useRequiredAccountBook } from '@/stores/accountBookStore';

const ReportMonthlySkeleton = () => {
  const { localCountryCode } = useRequiredAccountBook();
  const localCountryName = getCountryInfo(localCountryCode)?.countryName ?? '';

  return (
    <ReportContainer title="월별 지출 비교">
      <ReportContent className="h-fit w-109 gap-8">
        <p className="headline1-bold text-label-neutral h-15">
          다른 {localCountryName} 교환학생의 데이터가 아직 충분하지 않아요
        </p>

        <div className="flex flex-col gap-3">
          <div className="flex h-8.5 items-center gap-3.5">
            <div className="bg-cool-neutral-95 h-8 w-48 rounded-sm" />
            <div className="flex h-full flex-col justify-between">
              <span className="caption2-medium text-cool-neutral-90">
                {localCountryName} 교환학생 평균
              </span>
              <span className="body2-normal-bold text-cool-neutral-90">
                ₩ ???
              </span>
            </div>
          </div>
          <div className="flex h-8.5 items-center gap-3.5">
            <div className="bg-cool-neutral-80 h-8 w-36 rounded-sm" />
            <div className="flex h-full flex-col justify-between">
              <span className="caption2-medium text-cool-neutral-80">나</span>
              <span className="body2-normal-bold text-cool-neutral-80">
                ₩ ???
              </span>
            </div>
          </div>
        </div>
      </ReportContent>
    </ReportContainer>
  );
};

export default ReportMonthlySkeleton;
