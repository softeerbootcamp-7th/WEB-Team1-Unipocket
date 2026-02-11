import { useState } from 'react';

import AnalyticsProvider from '@/components/analytics-page/AnalyticsProvider';
import ReportCategory from '@/components/analytics-page/category/ReportCategory';

import { type CurrencyType } from '@/types/currency';

import mockData from '../components/analytics-page/mock';

const AnalyticsPage = () => {
  const categoryData = mockData.compareByCategory;
  const [currencyType, setCurrencyType] = useState<CurrencyType>('LOCAL'); // @TODO: 드롭다운 추가 예정

  return (
    <div className="flex p-3">
      <AnalyticsProvider currencyType={currencyType} onCurrencyTypeChange={setCurrencyType}>
        <ReportCategory data={categoryData} />
      </AnalyticsProvider>
    </div>
  );
};

export default AnalyticsPage;
