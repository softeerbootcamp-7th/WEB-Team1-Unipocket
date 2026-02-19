import { useState } from 'react';

import BudgetChartSkeleton from '@/components/chart/budget/BudgetChartSkeleton';
import BudgetChartView from '@/components/chart/budget/BudgetChartView';
import { mockData } from '@/components/chart/budget/mock';
import type { ChartMode } from '@/components/chart/chartType';
import ChartContainer from '@/components/chart/layout/ChartContainer';
import ChartContent from '@/components/chart/layout/ChartContent';
import ChartHeader from '@/components/chart/layout/ChartHeader';
import Button from '@/components/common/Button';
import BudgetSetupModal from '@/components/modal/BudgetSetupModal';

const BudgetChart = ({ isPreview = false }: ChartMode) => {
  // 렌더링용 데이터. API 연동 시 변경 필요
  const { totalBudget, usedBudget } = mockData;
  const [isModalOpen, setIsModalOpen] = useState(false);

  return (
    <ChartContainer isPreview={isPreview}>
      <ChartHeader title="남은 예산">
        <Button size="2xs" onClick={() => setIsModalOpen(true)}>
          설정
        </Button>
      </ChartHeader>
      <ChartContent
        className="w-full p-5"
        isPreview={isPreview}
        skeleton={<BudgetChartSkeleton />}
      >
        <BudgetChartView totalBudget={totalBudget} usedBudget={usedBudget} />
      </ChartContent>
      <BudgetSetupModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
      />
    </ChartContainer>
  );
};

export default BudgetChart;
