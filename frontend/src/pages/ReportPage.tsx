import { useState } from 'react';

import Switch from '@/components/common/Switch';
import ReportCategory from '@/components/report-page/category/ReportCategory';
import mockData from '@/components/report-page/mock';
import ReportMonthly from '@/components/report-page/monthly/ReportMonthly';
import ReportMyself from '@/components/report-page/myself/ReportMyself';
import ReportProvider from '@/components/report-page/ReportProvider';

import { type CurrencyType } from '@/types/currency';

import { Icons } from '@/assets';

const ReportPage = () => {
  const year = new Date().getFullYear();
  const month = new Date().getMonth() + 1;
  const categoryData = mockData.compareByCategory;
  const myselfData = mockData.compareWithLastMonth;
  const [currencyType, setCurrencyType] = useState<CurrencyType>('LOCAL'); // @TODO: 드롭다운 추가 예정

  return (
    <div className="flex min-w-283 flex-col gap-9.5 px-30 pt-8">
      <div className="flex flex-col gap-3">
        <span className="title2-bold text-label-normal">분석</span>
        <span className="headline1-medium text-label-alternative">
          내 월별 소비를 같은 국가 교환학생과 비교하고 전월 대비 현재 지출
          변화를 살펴봐요
        </span>
      </div>
      <div className="flex min-w-283 flex-col gap-4.75">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-[21.5px]">
            <span className="title3-medium text-label-normal">
              {year}년 {month}월
            </span>
            <div className="flex gap-3.5">
              <Icons.ChevronBack className="text-label-neutral size-6" />
              <Icons.ChevronForward className="text-label-neutral size-6" />
            </div>
          </div>
          <Switch
            checked={currencyType === 'LOCAL'}
            onChange={(checked) => setCurrencyType(checked ? 'LOCAL' : 'BASE')}
          />
        </div>

        <div className="flex w-full min-w-283 gap-3.5">
          <ReportProvider
            currencyType={currencyType}
            onCurrencyTypeChange={setCurrencyType}
          >
            <div className="flex w-113 flex-col justify-between">
              <ReportMonthly />
              <ReportMyself data={myselfData} />
            </div>

            <div className="flex-1">
              <ReportCategory data={categoryData} />
            </div>
          </ReportProvider>
        </div>
      </div>
    </div>
  );
};

export default ReportPage;
