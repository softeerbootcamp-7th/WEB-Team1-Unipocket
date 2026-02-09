import { useState } from 'react';

import { CURRENCY_OPTIONS } from '@/components/common/chart/chartType';
import ComparisonChartSkeleton from '@/components/common/chart/comparison/ComparisonChartSkeleton';
import ComparisonChartView from '@/components/common/chart/comparison/ComparisonChartView';
import ChartContainer from '@/components/common/chart/layout/ChartContainer';
import ChartContent from '@/components/common/chart/layout/ChartContent';
import ChartHeader from '@/components/common/chart/layout/ChartHeader';
import DropDown from '@/components/common/dropdown/Dropdown';

const ComparisonChart = ({ isLoading = false }: { isLoading?: boolean }) => {
  const [selectedCurrency, setSelectedCurrency] = useState<number>(
    CURRENCY_OPTIONS[0].id,
  );

  return (
    <ChartContainer className="w-67">
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
        isLoading={isLoading}
      >
        <ComparisonChartView selectedId={selectedCurrency} />
      </ChartContent>
    </ChartContainer>
  );
};

export default ComparisonChart;
