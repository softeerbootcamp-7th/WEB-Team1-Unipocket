import { useState } from 'react';

import DropDown from '@/components/common/dropdown/Dropdown';

import type { CategoryType } from '@/types/category';

import { CATEGORY_COLORS } from '../chartColor';
import {
  CategoryListItemSkeleton,
  CategoryPieChartSkeleton,
} from './CategoryChartSkeleton';
import CategoryListItem from './CategoryListItem';
import CategoryPieChart from './CategoryPieChart';
import { mockData } from './mock';

const PERIOD_OPTIONS = [
  { id: 1, name: '전체' },
  { id: 2, name: '월별' },
];

type CategoryStatisticsItem = {
  categoryName: CategoryType;
  amount: number;
  percent: number;
};

export type CategoryStatisticsResponse = {
  totalAmount: number;
  countryCode: CountryCode;
  items: CategoryStatisticsItem[];
};

// 임시용
const totalAmount = (amount: string) => {
  return <div>{amount}</div>;
};

const StatSectionSkeleton = () => {
  return (
    <>
      <CategoryPieChartSkeleton />
      <div className="flex flex-col justify-between">
        {Array.from({ length: 7 }).map((_, idx) => (
          <CategoryListItemSkeleton key={idx} />
        ))}
      </div>
    </>
  );
};

const CategoryChart = ({ isLoading = false }: { isLoading?: boolean }) => {
  const [selectedCurrency, setSelectedCurrency] = useState(
    CURRENCY_OPTIONS[0].id,
  );
  const [selectedPeriod, setSelectedPeriod] = useState(PERIOD_OPTIONS[0].id);

  // 실제 데이터 렌더링용 (API 응답 구조에 맞춤)
  const categoryStats = mockData.items.map((item) => ({
    percentage: item.percent,
    categoryName: item.categoryName,
    amount: item.amount.toLocaleString(),
  }));

  const formattedTotalAmount = `₩${dummyData.totalAmount.toLocaleString()}`;

  return (
    <div className="rounded-modal-16 bg-background-normal shadow-semantic-subtle flex w-139 flex-col gap-2.5 p-2 pt-4">
      {/* header */}
      <div className="flex items-center justify-between px-2.5">
        <span>카테고리별 지출</span>
        <div className="flex items-center gap-1.5">
          <DropDown
            selected={selectedCurrency}
            onSelect={setSelectedCurrency}
            options={CURRENCY_OPTIONS}
            size="xs"
          />
          <DropDown
            selected={selectedPeriod}
            onSelect={setSelectedPeriod}
            options={PERIOD_OPTIONS}
            size="xs"
          />
        </div>
      </div>
      {/* stat section */}
      <div className="rounded-modal-8 bg-background-alternative flex justify-between px-8 py-4">
        {isLoading ? (
          <StatSectionSkeleton />
        ) : (
          <>
            <CategoryPieChart
              data={chartData}
              totalAmount={totalAmount(formattedTotalAmount)}
            />
            <div className="flex flex-col justify-between">
              {chartData.map(
                (item, idx) =>
                  item.percentage > 0 && (
                    <CategoryListItem
                      key={item.categoryName}
                      currencyType={
                        CURRENCY_OPTIONS.find(
                          (opt) => opt.id === selectedCurrency,
                        )?.type || 'BASE'
                      }
                      countryCode={dummyData.countryCode}
                      categoryName={item.categoryName}
                      percentage={item.percentage}
                      amount={item.amount}
                      color={CATEGORY_COLORS[idx % CATEGORY_COLORS.length]}
                    />
                  ),
              )}
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default CategoryChart;
