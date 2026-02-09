import Button from '@/components/common/Button';
import { mockData } from '@/components/common/chart/budget/mock';
import ChartContainer from '@/components/common/chart/layout/ChartContainer';
import ChartContent from '@/components/common/chart/layout/ChartContent';
import ChartHeader from '@/components/common/chart/layout/ChartHeader';

import BudgetChartSkeleton from './BudgetChartSkeleton';
import BudgetChartView from './BudgetChartView';

const BudgetChart = ({ isLoading = false }: { isLoading?: boolean }) => {
  // 렌더링용 데이터. API 연동 시 변경 필요
  const { totalBudget, usedBudget } = mockData;

  return (
    <ChartContainer>
      <ChartHeader title="남은 예산">
        <Button size="xxs">설정</Button>
      </ChartHeader>
      <ChartContent
        className="w-full p-5"
        isLoading={isLoading}
        skeleton={<BudgetChartSkeleton />}
      >
        <BudgetChartView totalBudget={totalBudget} usedBudget={usedBudget} />
      </ChartContent>
    </ChartContainer>
  );
};

export default BudgetChart;
