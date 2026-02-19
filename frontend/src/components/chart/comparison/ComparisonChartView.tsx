import ComparisonCard from '@/components/chart/comparison/ComparisonCard';

import type { CountryCode } from '@/data/countryCode';
import { formatCurrencyAmount, getCountryInfo } from '@/lib/country';

interface ComparisonChartViewProps {
  month: number;
  countryCode: CountryCode;
  average: number;
  me: number;
  isLocal: boolean;
  localCountryCode: CountryCode;
}

const barWidth = {
  large: 'w-21.25',
  small: 'w-17',
  equal: 'w-19',
} as const;

const ComparisonChartView = ({
  month,
  countryCode,
  average,
  me,
  isLocal,
  localCountryCode,
}: ComparisonChartViewProps) => {
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
    <>
      {!isEqual && (
        <p className="body1-normal-bold text-label-neutral">
          나랑 같은 국가의 교환학생보다 <br />
          <span className="text-primary-strong">
            {formattedDiff}
            {unit} {isLess ? '덜' : '더'}
          </span>{' '}
          썼어요
        </p>
      )}
      {isEqual && (
        <p className="body1-normal-bold text-label-neutral">
          <span className="text-primary-strong">평균</span>과 일치해요!
        </p>
      )}

      <div className="flex h-26.5 flex-col gap-3">
        <span className="caption2-medium text-label-assistive">
          기준 : {month}월
        </span>
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
    </>
  );
};

export default ComparisonChartView;
