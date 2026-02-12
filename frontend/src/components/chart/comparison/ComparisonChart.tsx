import { useState } from 'react';

import { type ChartMode, CURRENCY_OPTIONS } from '@/components/chart/chartType';
import ComparisonChartSkeleton from '@/components/chart/comparison/ComparisonChartSkeleton';
import ComparisonChartView from '@/components/chart/comparison/ComparisonChartView';
import ChartContainer from '@/components/chart/layout/ChartContainer';
import ChartContent from '@/components/chart/layout/ChartContent';
import ChartHeader from '@/components/chart/layout/ChartHeader';
import DropDown from '@/components/common/dropdown/Dropdown';

const ComparisonChart = ({ isPreview = false }: ChartMode) => {
  const [selectedCurrency, setSelectedCurrency] = useState<number>(
    CURRENCY_OPTIONS[0].id,
  );

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
        isPreview={isPreview}
      >
        <ComparisonChartView selectedId={selectedCurrency} />
      </ChartContent>
    </ChartContainer>
  );
};

export default ComparisonChart;
