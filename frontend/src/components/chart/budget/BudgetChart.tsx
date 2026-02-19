import { useState } from 'react';

import BudgetChartSkeleton from '@/components/chart/budget/BudgetChartSkeleton';
import BudgetChartView from '@/components/chart/budget/BudgetChartView';
import type { ChartMode } from '@/components/chart/chartType';
import ChartContainer from '@/components/chart/layout/ChartContainer';
import ChartContent from '@/components/chart/layout/ChartContent';
import ChartHeader from '@/components/chart/layout/ChartHeader';
import Button from '@/components/common/Button';
import BudgetSetupModal from '@/components/modal/BudgetSetupModal';

import { useWidgetQuery } from '@/api/widget/query';
import type { BudgetWidgetResponse } from '@/api/widget/type';

const BudgetChart = ({ isPreview = false }: ChartMode) => {
  const { data, isLoading } = useWidgetQuery<BudgetWidgetResponse>('BUDGET');
  const [isModalOpen, setIsModalOpen] = useState(false);

  const showSkeleton = isPreview || isLoading || !data;

  return (
    <ChartContainer isPreview={isPreview}>
      <ChartHeader title="남은 예산">
        <Button size="2xs" onClick={() => setIsModalOpen(true)}>
          설정
        </Button>
      </ChartHeader>
      <ChartContent
        className="w-full p-5"
        isPreview={showSkeleton}
        skeleton={<BudgetChartSkeleton />}
      >
        {data && (
          <BudgetChartView
            totalBudget={Number(data.budget)}
            usedBudget={Number(data.baseSpentAmount)}
            localUsedBudget={Number(data.localSpentAmount)}
            baseCode={data.baseCountryCode}
            localCode={data.localCountryCode}
          />
        )}
      </ChartContent>
      <BudgetSetupModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
      />
    </ChartContainer>
  );
};

export default BudgetChart;
