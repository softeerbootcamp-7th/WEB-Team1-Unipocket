import { useMemo, useState } from 'react';

import CategoryChartSkeleton from '@/components/chart/category/CategoryChartSkeleton';
import CategoryChartView from '@/components/chart/category/CategoryChartView';
import { mockData } from '@/components/chart/category/mock';
import { type ChartMode, CURRENCY_OPTIONS } from '@/components/chart/chartType';
import ChartContainer from '@/components/chart/layout/ChartContainer';
import ChartContent from '@/components/chart/layout/ChartContent';
import ChartHeader from '@/components/chart/layout/ChartHeader';
import DropDown from '@/components/common/dropdown/Dropdown';

const PERIOD_OPTIONS = [
  { id: 1, name: '전체' },
  { id: 2, name: '월별' },
];

const CategoryChart = ({ isPreview = false }: ChartMode) => {
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

  return (
    <ChartContainer className="w-139" isPreview={isPreview}>
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
      <ChartContent
        isPreview={isPreview || visibleStats.length === 0}
        skeleton={<CategoryChartSkeleton />}
        className="px-8 py-4"
      >
        <CategoryChartView
          data={visibleStats}
          totalAmount={mockData.totalAmount}
          currencyType={
            CURRENCY_OPTIONS.find((opt) => opt.id === selectedCurrency)?.type ||
            'BASE'
          }
          countryCode={mockData.countryCode}
        />
      </ChartContent>
    </ChartContainer>
  );
};

export default CategoryChart;
