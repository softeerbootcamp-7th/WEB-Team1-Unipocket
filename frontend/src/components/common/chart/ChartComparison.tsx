import { useState } from 'react';

import DropDown from '@/components/common/dropdown/Dropdown';

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

const ChartComparison = () => {
  const [selectedId, setSelectedId] = useState<number | null>(1);
  const isBaseCurrency = selectedId === 1;

  const options = [
    { id: 1, name: '기준 통화' },
    { id: 2, name: '현지 통화' },
  ];

  const data = isBaseCurrency ? DUMMY_DATA.base : DUMMY_DATA.local;

  return (
    <div className="shadow-semantic-subtle bg-background-normal flex h-67 w-67 flex-col gap-2.5 rounded-2xl px-2 pt-4 pb-2">
      <div className="flex items-center justify-between px-2.5">
        <span className="body2-normal-medium text-label-neutral">
          내 월간 소비 비교
        </span>
        <div className="flex">
          <DropDown
            selected={selectedId}
            options={options}
            size="xs"
            align="center"
            onSelect={setSelectedId}
            itemWidth="w-18.5"
          />
        </div>
      </div>
      <div className="bg-background-alternative rounded-modal-8 flex h-56.5 flex-col justify-between px-4 py-5">
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
      </div>
    </div>
  );
};

export default ChartComparison;
