import { useEffect, useState } from 'react';

import ReportCategory from '@/components/report-page/category/ReportCategory';
import mockData from '@/components/report-page/mock';
import ReportMonthly from '@/components/report-page/monthly/ReportMonthly';
import ReportMyself from '@/components/report-page/myself/ReportMyself';
import ReportProvider from '@/components/report-page/ReportProvider';

import { type CurrencyType } from '@/types/currency';

import { mockData as accountBookMock } from '@/stores/mock';
import { useAccountBookStore } from '@/stores/useAccountBookStore';

const ReportPage = () => {
  const accountBook = useAccountBookStore((state) => state.accountBook);
  const categoryData = mockData.compareByCategory;
  const myselfData = mockData.compareWithLastMonth;
  const [currencyType, setCurrencyType] = useState<CurrencyType>('LOCAL'); // @TODO: 드롭다운 추가 예정

  // 추후 삭제될 예정
  useEffect(() => {
    if (!accountBook) {
      console.log('Setting mock account book data');
      useAccountBookStore.getState().setAccountBook(accountBookMock);
    }
  }, [accountBook]);

  return (
    <div className="flex gap-3.5 p-3">
      <ReportProvider currencyType={currencyType} onCurrencyTypeChange={setCurrencyType}>
        <div className="flex flex-col justify-between" >
          <ReportMonthly />
          <ReportMyself data={myselfData} />
        </div>
        <ReportCategory data={categoryData} />
      </ReportProvider>
    </div>
  );
};

export default ReportPage;
