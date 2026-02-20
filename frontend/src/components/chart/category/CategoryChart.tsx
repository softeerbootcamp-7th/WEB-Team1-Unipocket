import { useMemo, useState } from 'react';

import { transformCategoryChartData } from '@/components/chart/category/category.utils';
import CategoryChartSkeleton from '@/components/chart/category/CategoryChartSkeleton';
import CategoryChartView from '@/components/chart/category/CategoryChartView';
import { type ChartMode, CURRENCY_OPTIONS } from '@/components/chart/chartType';
import ChartContainer from '@/components/chart/layout/ChartContainer';
import ChartContent from '@/components/chart/layout/ChartContent';
import ChartHeader from '@/components/chart/layout/ChartHeader';
import { CATEGORY_PERIOD_OPTIONS } from '@/components/chart/widgetPeriod';
import DropDown from '@/components/common/dropdown/Dropdown';

import type { CurrencyType } from '@/types/currency';
import { getPeriodTypeById, type PeriodType } from '@/types/period';

import { useWidgetQuery } from '@/api/widget/query';

const CategoryChart = ({ isPreview = false }: ChartMode) => {
  const [selectedCurrency, setSelectedCurrency] = useState(
    CURRENCY_OPTIONS[0].id,
  );
  const [selectedPeriod, setSelectedPeriod] = useState(
    CATEGORY_PERIOD_OPTIONS[0].id,
  );

  const currencyType: CurrencyType =
    CURRENCY_OPTIONS.find((opt) => opt.id === selectedCurrency)?.type || 'BASE';
  const periodType: PeriodType = getPeriodTypeById(selectedPeriod);

  const { data, isLoading } = useWidgetQuery('CATEGORY', {
    currencyType,
    period: periodType,
  });

  const visibleStats = useMemo(() => transformCategoryChartData(data), [data]);

  const showSkeleton =
    isPreview || isLoading || !data || visibleStats.length === 0;

  return (
    <ChartContainer className="w-139" isPreview={isPreview}>
      <ChartHeader title="카테고리별 지출">
        <DropDown
          selectedId={selectedCurrency}
          onSelect={setSelectedCurrency}
          options={CURRENCY_OPTIONS}
          size="xs"
        />
        <DropDown
          selectedId={selectedPeriod}
          onSelect={setSelectedPeriod}
          options={CATEGORY_PERIOD_OPTIONS}
          size="xs"
        />
      </ChartHeader>

      {/* stat section */}
      <ChartContent
        isPreview={showSkeleton}
        skeleton={<CategoryChartSkeleton />}
        className="px-8 py-4"
      >
        {data && visibleStats.length > 0 && (
          <CategoryChartView
            key={`${currencyType}-${periodType}`}
            data={visibleStats}
            totalAmount={Number(data.totalAmount)}
            currencyType={currencyType}
            countryCode={data.countryCode}
          />
        )}
      </ChartContent>
    </ChartContainer>
  );
};

export default CategoryChart;
