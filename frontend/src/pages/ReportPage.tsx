import { useState } from 'react';

import ReportCategory from '@/components/report-page/category/ReportCategory';
import mockData from '@/components/report-page/mock';
import ReportMyself from '@/components/report-page/myself/ReportMyself';
import ReportProvider from '@/components/report-page/ReportProvider';

import { type CurrencyType } from '@/types/currency';

const ReportPage = () => {
  const categoryData = mockData.compareByCategory;
  const myselfData = mockData.compareWithLastMonth;
  const [currencyType, setCurrencyType] = useState<CurrencyType>('LOCAL'); // @TODO: 드롭다운 추가 예정

  return (
    <div className="flex p-3 gap-5">
      <ReportProvider currencyType={currencyType} onCurrencyTypeChange={setCurrencyType}>
        <ReportMyself data={myselfData} />
        <ReportCategory data={categoryData} />
      </ReportProvider>
    </div>
  );
};

export default ReportPage;
