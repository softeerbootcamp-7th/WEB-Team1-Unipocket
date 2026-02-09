import CurrencyAmountDisplay from '@/components/common/currency/CurrencyAmountDisplay';
import CurrencyBadge from '@/components/common/currency/CurrencyBadge';

import { type CurrencyType } from '@/types/currency';

import countryData from '@/data/countryData.json';
import { formatCurrencyAmount } from '@/lib/country';

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
        <div className="flex h-8.5 gap-3.5">
          <div
            className={`bg-cool-neutral-95 h-8 ${averageBarWidth} items-center rounded-xs animate-expand-width origin-left`}
          />
          <div className="flex flex-col gap-1.5">
            <span className="text-cool-neutral-80 caption2-medium">
              미국 교환학생 평균
            </span>
            <div className="flex items-center gap-1.75">
              {isLocal && (
                <CurrencyBadge
                  countryCode={data.countryCode}
                  className="text-cool-neutral-70"
                />
              )}
              <CurrencyAmountDisplay
                countryCode={data.countryCode}
                amount={data.average}
                size="sm"
                className="text-cool-neutral-70"
              />
            </div>
          </div>
        </div>
        <div className="flex h-8.5 gap-3.5">
          <div
            className={`bg-primary-normal h-8 ${meBarWidth} items-center rounded-xs animate-expand-width origin-left`}
          />
          <div className="flex flex-col gap-1.5">
            <span className="text-primary-normal caption2-medium">나</span>
            <div className="flex items-center gap-1.75">
              {isLocal && (
                <CurrencyBadge
                  countryCode={data.countryCode}
                  className="text-primary-normal"
                />
              )}
              <CurrencyAmountDisplay
                countryCode={data.countryCode}
                amount={data.me}
                size="sm"
                className="text-primary-normal"
              />
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default ComparisonChartView;
