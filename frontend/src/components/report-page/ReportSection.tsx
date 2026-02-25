import ReportCategory from '@/components/report-page/category/ReportCategory';
import ReportCategorySkeleton from '@/components/report-page/category/ReportCategorySkeleton';
import ReportMonthly from '@/components/report-page/monthly/ReportMonthly';
import ReportMyself from '@/components/report-page/myself/ReportMyself';
import ReportProvider from '@/components/report-page/ReportProvider';

import { type CurrencyType } from '@/types/currency';

import { type GetAnalysisResponse } from '@/api/account-books/type';

interface ReportSectionProps {
  data: GetAnalysisResponse;
  currencyType: CurrencyType;
  onCurrencyTypeChange: (type: CurrencyType) => void;
  isCurrentMonth: boolean;
}

const ReportSection = ({
  data,
  currencyType,
  onCurrencyTypeChange,
  isCurrentMonth,
}: ReportSectionProps) => {
  const categoryItems = data.compareByCategory.items;
  const allMyZero = categoryItems.every(
    (item) => Number(item.mySpentAmount) === 0,
  );
  const allOtherZero = categoryItems.every(
    (item) => Number(item.averageSpentAmount) === 0,
  );
  const showCategorySkeleton = allMyZero || allOtherZero;

  return (
    <div className="flex w-full min-w-283 gap-3.5">
      <ReportProvider
        currencyType={currencyType}
        onCurrencyTypeChange={onCurrencyTypeChange}
      >
        <div className="flex w-113 flex-col justify-between">
          <ReportMonthly data={data.compareWithAverage} />
          <ReportMyself
            data={data.compareWithLastMonth}
            isCurrentMonth={isCurrentMonth}
          />
        </div>

        <div className="flex-1">
          {showCategorySkeleton ? (
            <ReportCategorySkeleton />
          ) : (
            <ReportCategory data={data.compareByCategory} />
          )}
        </div>
      </ReportProvider>
    </div>
  );
};

export default ReportSection;
