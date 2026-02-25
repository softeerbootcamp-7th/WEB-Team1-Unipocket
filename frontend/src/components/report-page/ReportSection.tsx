import ReportCategory from '@/components/report-page/category/ReportCategory';
import ReportCategorySkeleton from '@/components/report-page/category/ReportCategorySkeleton';
import ReportMonthly from '@/components/report-page/monthly/ReportMonthly';
import ReportMonthlySkeleton from '@/components/report-page/monthly/ReportMonthlySkeleton';
import ReportMyself from '@/components/report-page/myself/ReportMyself';
import ReportMyselfSkeleton from '@/components/report-page/myself/ReportMyselfSkeleton';
import ReportProvider from '@/components/report-page/ReportProvider';

import { type CurrencyType } from '@/types/currency';

import { type GetAnalysisResponse } from '@/api/account-books/type';

interface ReportSectionProps {
  data: GetAnalysisResponse;
  currencyType: CurrencyType;
  onCurrencyTypeChange: (type: CurrencyType) => void;
  isCurrentMonth: boolean;
  isPlaceholderData: boolean;
}

const ReportSection = ({
  data,
  currencyType,
  onCurrencyTypeChange,
  isCurrentMonth,
  isPlaceholderData,
}: ReportSectionProps) => {
  // monthly 그래프 스켈레톤 노출 조건
  const monthlyData = data.compareWithAverage;
  const myMonthlyZero = Number(monthlyData.mySpentAmount) === 0;
  const otherMonthlyZero = Number(monthlyData.averageSpentAmount) === 0;
  const showMonthlySkeleton = myMonthlyZero || otherMonthlyZero;
  const monthlySkeletonReason: 'me' | 'other' = myMonthlyZero ? 'me' : 'other';

  // category 그래프 스켈레톤 노출 조건
  const categoryItems = data.compareByCategory.items;
  const allMyZero = categoryItems.every(
    (item) => Number(item.mySpentAmount) === 0,
  );
  const allOtherZero = categoryItems.every(
    (item) => Number(item.averageSpentAmount) === 0,
  );
  const showCategorySkeleton = allMyZero || allOtherZero;
  const categorySkeletonReason: 'me' | 'other' = allMyZero ? 'me' : 'other';

  // myself 그래프 스켈레톤 노출 조건
  const myselfData = data.compareWithLastMonth;
  const showMyselfSkeleton =
    Number(myselfData.totalSpent.thisMonthToDate) === 0;

  return (
    <div className="flex w-full min-w-283 gap-3.5">
      <ReportProvider
        currencyType={currencyType}
        onCurrencyTypeChange={onCurrencyTypeChange}
      >
        <div className="flex w-113 flex-col justify-between">
          {showMonthlySkeleton ? (
            <ReportMonthlySkeleton reason={monthlySkeletonReason} />
          ) : (
            <ReportMonthly
              data={monthlyData}
              isPlaceholderData={isPlaceholderData}
            />
          )}
          {showMyselfSkeleton ? (
            <ReportMyselfSkeleton />
          ) : (
            <ReportMyself
              data={myselfData}
              isCurrentMonth={isCurrentMonth}
              isPlaceholderData={isPlaceholderData}
            />
          )}
        </div>

        <div className="flex-1">
          {showCategorySkeleton ? (
            <ReportCategorySkeleton reason={categorySkeletonReason} />
          ) : (
            <ReportCategory data={data.compareByCategory} />
          )}
        </div>
      </ReportProvider>
    </div>
  );
};

export default ReportSection;
