import { useState } from 'react';

import BudgetChartSkeleton from '@/components/chart/budget/BudgetChartSkeleton';
import BudgetChartView from '@/components/chart/budget/BudgetChartView';
import type { ChartMode } from '@/components/chart/chartType';
import ChartContainer from '@/components/chart/layout/ChartContainer';
import ChartContent from '@/components/chart/layout/ChartContent';
import ChartHeader from '@/components/chart/layout/ChartHeader';
import { CHART_MESSAGES } from '@/components/chart/message';
import Button from '@/components/common/Button';
import BudgetSetupModal from '@/components/modal/BudgetSetupModal';

import { useWidgetQuery } from '@/api/widget/query';

const BudgetChart = ({ isPreview = false }: ChartMode) => {
  const { data, isLoading } = useWidgetQuery('BUDGET');
  const [isModalOpen, setIsModalOpen] = useState(false);

  const budgetAmount = Number(data?.budget) || 0;
  const usedAmount = Number(data?.baseSpentAmount) || 0;
  const localUsedAmount = Number(data?.localSpentAmount) || 0;

  const showSkeleton = isPreview || isLoading || !data;
  const isEmpty = !showSkeleton && budgetAmount === 0;

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
        isEmpty={isEmpty}
        emptyMessage={CHART_MESSAGES.BUDGET_EMPTY}
        skeleton={<BudgetChartSkeleton />}
      >
        {data && (
          <BudgetChartView
            totalBudget={budgetAmount}
            usedBudget={usedAmount}
            localUsedBudget={localUsedAmount}
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
