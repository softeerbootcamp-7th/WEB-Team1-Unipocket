import { useState } from 'react';

import { type ChartMode, CURRENCY_OPTIONS } from '@/components/chart/chartType';
import ComparisonChartSkeleton from '@/components/chart/comparison/ComparisonChartSkeleton';
import ComparisonChartView from '@/components/chart/comparison/ComparisonChartView';
import ChartContainer from '@/components/chart/layout/ChartContainer';
import ChartContent from '@/components/chart/layout/ChartContent';
import ChartHeader from '@/components/chart/layout/ChartHeader';
import DropDown from '@/components/common/dropdown/Dropdown';

import type { CurrencyType } from '@/types/currency';

import { useWidgetQuery } from '@/api/widget/query';
import type { ComparisonWidgetResponse } from '@/api/widget/type';
import type { CountryCode } from '@/data/countryCode';
import { useAccountBookStore } from '@/stores/useAccountBookStore';

const ComparisonChart = ({ isPreview = false }: ChartMode) => {
  const [selectedCurrency, setSelectedCurrency] = useState<number>(
    CURRENCY_OPTIONS[0].id,
  );

  const currencyType: CurrencyType = selectedCurrency === 1 ? 'BASE' : 'LOCAL';
  const { data, isLoading } = useWidgetQuery<ComparisonWidgetResponse>(
    'COMPARISON',
    { currencyType },
  );

  const localCountryCode = useAccountBookStore(
    (state) => state.accountBook?.localCountryCode,
  ) as CountryCode | undefined;

  const showSkeleton = isPreview || isLoading || !data;

  return (
    <ChartContainer className="w-67" isPreview={isPreview}>
      <ChartHeader title="내 월간 소비 비교">
        <DropDown
          selected={selectedCurrency}
          options={CURRENCY_OPTIONS}
          size="xs"
          align="center"
          onSelect={setSelectedCurrency}
          itemWidth="w-20"
        />
      </ChartHeader>
      <ChartContent
        className="h-56.5 flex-col px-4 py-5"
        skeleton={<ComparisonChartSkeleton />}
        isPreview={showSkeleton}
      >
        {data && (
          <ComparisonChartView
            month={data.month}
            countryCode={data.countryCode}
            average={Number(data.averageSpentAmount)}
            me={Number(data.mySpentAmount)}
            isLocal={currencyType === 'LOCAL'}
            localCountryCode={localCountryCode ?? 'US'}
          />
        )}
      </ChartContent>
    </ChartContainer>
  );
};

export default ComparisonChart;
