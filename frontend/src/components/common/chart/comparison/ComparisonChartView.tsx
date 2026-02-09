import { type CurrencyType } from '@/types/currency';

import countryData from '@/data/countryData.json';
import { formatCurrencyAmount } from '@/lib/country';

import ComparisonCard from './ComparisonCard';
import { mockData } from './mock';
interface ComparisonChartViewProps {
  selectedId: number;
}

const barWidth = {
  large: 'w-21.25',
  small: 'w-17',
  equal: 'w-19',
} as const;

const ComparisonChartView = ({ selectedId }: ComparisonChartViewProps) => {
  const selectedCurrency: CurrencyType = selectedId === 1 ? 'BASE' : 'LOCAL';
  const data = selectedCurrency === 'BASE' ? mockData.base : mockData.local;
  const isLocal = selectedCurrency === 'LOCAL';

  const diff = Math.abs(data.average - data.me);
  const isLess = data.me < data.average;
  const isEqual = data.me === data.average;

  const unit = countryData[data.countryCode].currencyUnitKor;
  const formattedDiff = formatCurrencyAmount(diff, data.countryCode, 0);

  const [averageBarWidth, meBarWidth] = isEqual
    ? [barWidth.equal, barWidth.equal]
    : data.average > data.me
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
          기준 : {mockData.month}월
        </span>
        <ComparisonCard
          barWidth={averageBarWidth}
          barColor="bg-cool-neutral-95"
          label="미국 교환학생 평균"
          amount={data.average}
          countryCode={data.countryCode}
          isLocal={isLocal}
          textColor="text-cool-neutral-80"
          amountTextColor="text-cool-neutral-70"
        />
        <ComparisonCard
          barWidth={meBarWidth}
          barColor="bg-primary-normal"
          label="나"
          amount={data.me}
          countryCode={data.countryCode}
          isLocal={isLocal}
          textColor="text-primary-normal"
          amountTextColor="text-primary-normal"
        />
      </div>
    </>
  );
};

export default ComparisonChartView;
