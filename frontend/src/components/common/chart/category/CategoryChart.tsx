import { useMemo, useState } from 'react';

import CurrencyAmountDisplay from '@/components/common/currency/CurrencyAmountDisplay';
import DropDown from '@/components/common/dropdown/Dropdown';

import type { CategoryType } from '@/types/category';

import type { CountryCode } from '@/data/countryCode';

import ChartContainer from '../ChartContainer';
import ChartContent from '../ChartContent';
import ChartHeader from '../ChartHeader';
import { CATEGORY_CHART_COLORS, CURRENCY_OPTIONS } from '../chartType';
import StatSectionSkeleton from './CategoryChartSkeleton';
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

const CategoryChart = ({ isLoading = false }: { isLoading?: boolean }) => {
  const [selectedCurrency, setSelectedCurrency] = useState(
    CURRENCY_OPTIONS[0].id,
  );
  const [selectedPeriod, setSelectedPeriod] = useState(PERIOD_OPTIONS[0].id);

  // 렌더링용 데이터. API 연동 시 변경 필요
  const visibleStats = useMemo(() => {
    return mockData.items
      .map((item) => ({
        percentage: item.percent,
        categoryName: item.categoryName,
        amount: item.amount,
      }))
      .filter((item) => item.percentage > 0);
  }, []);

  const totalAmount = (
    <CurrencyAmountDisplay
      countryCode={mockData.countryCode}
      amount={mockData.totalAmount}
      size="lg"
    />
  );

  return (
    <ChartContainer className="w-139">
      <ChartHeader title="카테고리별 지출">
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
      </ChartHeader>

      {/* stat section */}
      <ChartContent isLoading={isLoading} skeleton={<StatSectionSkeleton />}>
        <CategoryPieChart data={visibleStats} totalAmount={totalAmount} />
        <div className="flex flex-col items-start justify-center gap-1">
          {visibleStats.map((item, idx) => (
            <CategoryListItem
              key={item.categoryName}
              currencyType={
                CURRENCY_OPTIONS.find((opt) => opt.id === selectedCurrency)
                  ?.type || 'BASE'
              }
              countryCode={mockData.countryCode}
              categoryName={item.categoryName}
              percentage={item.percentage}
              amount={item.amount}
              color={CATEGORY_CHART_COLORS[idx % CATEGORY_CHART_COLORS.length]}
            />
          ))}
        </div>
      </ChartContent>
    </ChartContainer>
  );
};

export default CategoryChart;
