import ComparisonCard from '@/components/chart/comparison/ComparisonCard';
import ReportContainer from '@/components/report-page/layout/ReportContainer';
import ReportContent from '@/components/report-page/layout/ReportContent';
import { useReportContext } from '@/components/report-page/ReportContext';

import { formatCurrencyAmount, getCountryInfo } from '@/lib/country';
import { useAccountBookStore } from '@/stores/useAccountBookStore';

const barWidth = {
  large: 'w-46',
  small: 'w-42',
  equal: 'w-38',
} as const;

interface ReportMonthlyProps {
  data: {
    mySpentAmount: string;
    averageSpentAmount: string;
    spentAmountDiff: string;
  };
}

const ReportMonthly = ({ data }: ReportMonthlyProps) => {
  const { currencyType } = useReportContext();
  const accountBook = useAccountBookStore((state) => state.accountBook);
  if (
    !accountBook ||
    !accountBook.localCountryCode ||
    !accountBook.baseCountryCode
  )
    return null;

  const { localCountryCode, baseCountryCode } = accountBook;
  const countryCode =
    currencyType === 'LOCAL' ? localCountryCode : baseCountryCode;

  const isLocal = currencyType === 'LOCAL';

  const me = Number(data.mySpentAmount);
  const average = Number(data.averageSpentAmount);

  const diff = Math.abs(average - me);
  const isLess = me < average;
  const isEqual = me === average;

  const unit = getCountryInfo(countryCode)?.currencyUnitKor || '';
  const localCountryName = getCountryInfo(localCountryCode)?.countryName || '';
  const formattedDiff = formatCurrencyAmount(diff, countryCode, 0);

  const [averageBarWidth, meBarWidth] = isEqual
    ? [barWidth.equal, barWidth.equal]
    : average > me
      ? [barWidth.large, barWidth.small]
      : [barWidth.small, barWidth.large];

  return (
    <ReportContainer title="월별 지출 비교">
      <ReportContent className="h-60 w-109 gap-7">
        <p className="heading1-bold text-label-normal">
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
            countryCode={countryCode}
            isLocal={isLocal}
          />
          <ComparisonCard
            variant="me"
            barWidth={meBarWidth}
            label="나"
            amount={me}
            countryCode={countryCode}
            isLocal={isLocal}
          />
        </div>
      </ReportContent>
    </ReportContainer>
  );
};

export default ReportMonthly;
