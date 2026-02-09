import { useState } from 'react';

import ComparisonChartView from '@/components/common/chart/comparison/ComparisonChartView';
import ChartContainer from '@/components/common/chart/layout/ChartContainer';
import ChartContent from '@/components/common/chart/layout/ChartContent';
import ChartHeader from '@/components/common/chart/layout/ChartHeader';
import DropDown from '@/components/common/dropdown/Dropdown';

const ComparisonChart = () => {
  const [selectedId, setSelectedId] = useState<number>(1);

  const options = [
    { id: 1, name: '기준 통화' },
    { id: 2, name: '현지 통화' },
  ];

  return (
    <ChartContainer className="w-67">
      <ChartHeader title="내 월간 소비 비교">
        <DropDown
          selected={selectedId}
          options={options}
          size="xs"
          align="center"
          onSelect={setSelectedId}
          itemWidth="w-20"
        />
      </ChartHeader>
      <ChartContent className="h-56.5 flex-col px-4 py-5">
        <ComparisonChartView selectedId={selectedId} />
      </ChartContent>
    </ChartContainer>
  );
};

export default ComparisonChart;
