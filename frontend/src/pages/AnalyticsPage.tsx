import CategoryProvider from '@/components/analytics-page/category/CategoryProvider';
import ReportCategory from '@/components/analytics-page/category/ReportCategory';

import { type CurrencyType } from '@/types/currency';

import mockData from '../components/analytics-page/mock.json';

const AnalyticsPage = () => {
  const categoryData = mockData.compareByCategory;
  const currencyType = 'LOCAL' as CurrencyType; // @TODO: 드롭다운 추가 예정 (임시로 고정)

  return (
    <div className="flex p-3">
      <CategoryProvider currencyType={currencyType}>
        <ReportCategory data={categoryData} />
      </CategoryProvider>
    </div>
  );
};

export default AnalyticsPage;
