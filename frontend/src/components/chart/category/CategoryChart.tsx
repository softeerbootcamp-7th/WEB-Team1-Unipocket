import { useMemo, useState } from 'react';

import CategoryChartSkeleton from '@/components/chart/category/CategoryChartSkeleton';
import CategoryChartView from '@/components/chart/category/CategoryChartView';
import { type ChartMode, CURRENCY_OPTIONS } from '@/components/chart/chartType';
import ChartContainer from '@/components/chart/layout/ChartContainer';
import ChartContent from '@/components/chart/layout/ChartContent';
import ChartHeader from '@/components/chart/layout/ChartHeader';
import { CATEGORY_PERIOD_OPTIONS } from '@/components/chart/widgetPeriod';
import DropDown from '@/components/common/dropdown/Dropdown';

import type { CurrencyType } from '@/types/currency';
import type { PeriodType } from '@/types/period';

import { useWidgetQuery } from '@/api/widget/query';
import type { CategoryWidgetResponse } from '@/api/widget/type';

const CategoryChart = ({ isPreview = false }: ChartMode) => {
  const [selectedCurrency, setSelectedCurrency] = useState(
    CURRENCY_OPTIONS[0].id,
  );
  const [selectedPeriod, setSelectedPeriod] = useState(
    CATEGORY_PERIOD_OPTIONS[0].id,
  );

  const currencyType: CurrencyType =
    CURRENCY_OPTIONS.find((opt) => opt.id === selectedCurrency)?.type || 'BASE';
  const periodType: PeriodType =
    CATEGORY_PERIOD_OPTIONS.find((opt) => opt.id === selectedPeriod)?.type ||
    'ALL';

  const { data, isLoading } = useWidgetQuery<CategoryWidgetResponse>(
    'CATEGORY',
    { currencyType, period: periodType },
  );

  const visibleStats = useMemo(() => {
    if (!data) return [];
    return data.items
      .map((item) => ({
        percentage: item.percent,
        categoryName: item.categoryName,
        amount: Number(item.amount),
      }))
      .filter((item) => item.percentage > 0);
  }, [data]);

  const showSkeleton = isPreview || isLoading || !data;

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
          options={CATEGORY_PERIOD_OPTIONS}
          size="xs"
        />
      </ChartHeader>

      {/* stat section */}
      <ChartContent
        isPreview={showSkeleton || visibleStats.length === 0}
        skeleton={<CategoryChartSkeleton />}
        className="px-8 py-4"
      >
        <CategoryChartView
          data={visibleStats}
          totalAmount={Number(data?.totalAmount ?? 0)}
          currencyType={currencyType}
          countryCode={data?.countryCode ?? 'KR'}
        />
      </ChartContent>
    </ChartContainer>
  );
};

export default CategoryChart;
