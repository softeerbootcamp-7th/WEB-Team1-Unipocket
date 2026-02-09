const DUMMY_DATA = {
  month: 12,
  base: {
    diffText: '31만원 덜',
    average: {
      currency: 'KRW',
      value: '₩ 2,311,465',
    },
    me: {
      currency: 'KRW',
      value: '₩ 2,002,876',
    },
  },
  local: {
    diffText: '234달러 덜',
    average: {
      currency: 'USD',
      value: '$ 2,234.00',
    },
    me: {
      currency: 'USD',
      value: '$ 2,000.00',
    },
  },
} as const;

interface ComparisonChartViewProps {
  selectedId: number;
}

import { type CurrencyType } from '@/types/currency';

const ComparisonChartView = ({ selectedId }: ComparisonChartViewProps) => {
  const selectedCurrency: CurrencyType = selectedId === 1 ? 'BASE' : 'LOCAL';
  const data = selectedCurrency === 'BASE' ? DUMMY_DATA.base : DUMMY_DATA.local;

  return (
    <>
      <p className="body1-normal-bold text-label-neutral">
        나랑 같은 국가의 교환학생보다 <br />
        <span className="text-primary-strong">{data.diffText} </span>
        썼어요
      </p>
      <div className="flex flex-col gap-3">
        <span className="caption2-medium text-label-assistive">
          기준 : {DUMMY_DATA.month}월
        </span>
        <div className="flex gap-3.5">
          <div className="bg-cool-neutral-95 h-8 w-21.25 rounded-xs" />
          <div className="flex flex-col gap-1.5">
            <span className="text-cool-neutral-80 caption2-medium">
              미국 교환학생 평균
            </span>
            <span className="text-cool-neutral-70 figure-body2-14-semibold">
              {data.average.value}
            </span>
          </div>
        </div>
        <div className="flex gap-3.5">
          <div className="bg-primary-normal h-8 w-17 rounded-xs" />
          <div className="flex flex-col gap-1.5">
            <span className="text-primary-normal caption2-medium">나</span>
            <span className="figure-body2-14-semibold text-primary-normal">
              {data.me.value}
            </span>
          </div>
        </div>
      </div>
    </>
  );
};

export default ComparisonChartView;
