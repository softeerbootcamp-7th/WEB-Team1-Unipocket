import ComparisonCard from '@/components/chart/comparison/ComparisonCard';
import ReportContainer from '@/components/report-page/layout/ReportContainer';
import ReportContent from '@/components/report-page/layout/ReportContent';
import { useReportContext } from '@/components/report-page/ReportContext';

import { formatAmountByCountry, getCountryInfo } from '@/lib/country';
import { useRequiredAccountBook } from '@/stores/accountBookStore';

const barWidth = {
  large: 'w-48',
  small: 'w-36',
  equal: 'w-42',
} as const;

interface ReportMonthlyProps {
  data: {
    mySpentAmount: string;
    averageSpentAmount: string;
    spentAmountDiff: string;
  };
  isPlaceholderData?: boolean;
}

const ReportMonthly = ({ data, isPlaceholderData }: ReportMonthlyProps) => {
  const { currencyType } = useReportContext();
  const { localCountryCode, baseCountryCode } = useRequiredAccountBook();
  const currentCountryCode =
    currencyType === 'LOCAL' ? localCountryCode : baseCountryCode;
  const displayCountryCode = isPlaceholderData
    ? baseCountryCode
    : currentCountryCode;
  const unit = getCountryInfo(displayCountryCode)?.currencyUnitKor || '';

  const localCountryName = getCountryInfo(localCountryCode)?.countryName || '';

  const isLocal = currencyType === 'LOCAL';

  const me = Number(data.mySpentAmount);
  const average = Number(data.averageSpentAmount);

  const diff = Math.abs(average - me);
  const isLess = me < average;
  const isEqual = me === average;

  const formattedDiff = formatAmountByCountry(diff, displayCountryCode, 0);

  const [averageBarWidth, meBarWidth] = isEqual
    ? [barWidth.equal, barWidth.equal]
    : average > me
      ? [barWidth.large, barWidth.small]
      : [barWidth.small, barWidth.large];

  return (
    <ReportContainer title="월별 지출 비교">
      <ReportContent className="h-fit w-109 gap-8">
        <p className="heading1-bold text-label-normal h-15">
          {!isEqual ? (
            <>
              나랑 같은 국가의 교환학생보다 <br />
              <span className="text-primary-strong">
                {formattedDiff}
                {unit} {isLess ? '덜' : '더'}
              </span>{' '}
              썼어요
            </>
          ) : (
            <>
              <span className="text-primary-strong">평균</span>과 일치해요!
            </>
          )}
        </p>

        <div className="flex flex-col gap-3">
          <ComparisonCard
            variant="average"
            barWidth={averageBarWidth}
            label={`${localCountryName} 교환학생 평균`}
            amount={average}
            countryCode={displayCountryCode}
            isLocal={isLocal}
          />
          <ComparisonCard
            variant="me"
            barWidth={meBarWidth}
            label="나"
            amount={me}
            countryCode={displayCountryCode}
            isLocal={isLocal}
          />
        </div>
      </ReportContent>
    </ReportContainer>
  );
};

export default ReportMonthly;
