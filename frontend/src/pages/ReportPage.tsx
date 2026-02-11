import { useState } from 'react';

import ReportCategory from '@/components/report-page/category/ReportCategory';
import mockData from '@/components/report-page/mock';
import ReportProvider from '@/components/report-page/ReportProvider';

import { type CurrencyType } from '@/types/currency';

const ReportPage = () => {
  const categoryData = mockData.compareByCategory;
  const [currencyType, setCurrencyType] = useState<CurrencyType>('LOCAL'); // @TODO: 드롭다운 추가 예정

  return (
    <div className="flex p-3">
      <ReportProvider
        currencyType={currencyType}
        onCurrencyTypeChange={setCurrencyType}
      >
        <ReportCategory data={categoryData} />
      </ReportProvider>
    </div>
  );
};

export default ReportPage;
