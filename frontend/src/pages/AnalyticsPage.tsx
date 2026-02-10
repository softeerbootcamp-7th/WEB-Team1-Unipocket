import CountryCodeProvider from '@/components/analytics-page/category/CountryCodeProvider';
import ReportCategory from '@/components/analytics-page/category/ReportCategory';

import { type CountryCode } from '@/data/countryCode';

import mockData from '../components/analytics-page/mock.json';

const AnalyticsPage = () => {
  const countryCode = mockData.countryCode as CountryCode;
  const categoryData = mockData.compareByCategory;

  return (
    <div className="flex p-3">
      <CountryCodeProvider value={countryCode}>
        <ReportCategory data={categoryData} />
      </CountryCodeProvider>
    </div>
  );
};

export default AnalyticsPage;
