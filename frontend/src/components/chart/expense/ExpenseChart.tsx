import { useMemo } from 'react';

import {
  type ChartMode,
  EXPENSE_TITLE_BY_MODE,
  type ExpenseChartMode,
} from '@/components/chart/chartType';
import {
  transformCurrencyChartData,
  transformPaymentChartData,
} from '@/components/chart/expense/expense.utils';
import ExpenseChartSkeleton from '@/components/chart/expense/ExpenseChartSkeleton';
import ExpenseChartView from '@/components/chart/expense/ExpenseChartView';
import ChartContainer from '@/components/chart/layout/ChartContainer';
import ChartContent from '@/components/chart/layout/ChartContent';
import ChartHeader from '@/components/chart/layout/ChartHeader';

import { useContextualWidgetQuery } from '@/api/widget/query';

interface ExpenseChartProps extends ChartMode {
  mode?: ExpenseChartMode;
}

const ExpenseChart = ({
  mode = 'method',
  isPreview = false,
}: ExpenseChartProps) => {
  const isMethod = mode === 'method';
  const { data: paymentData, isLoading: isPaymentLoading } =
    useContextualWidgetQuery('PAYMENT', { enabled: isMethod });
  const { data: currencyData, isLoading: isCurrencyLoading } =
    useContextualWidgetQuery('CURRENCY', {
      enabled: !isMethod,
    });

  const isLoading = isMethod ? isPaymentLoading : isCurrencyLoading;

  const chartData = useMemo(() => {
    return isMethod
      ? transformPaymentChartData(paymentData)
      : transformCurrencyChartData(currencyData);
  }, [isMethod, paymentData, currencyData]);

  const showSkeleton = isPreview || isLoading;
  const isEmpty = !showSkeleton && chartData.length === 0;

  return (
    <ChartContainer className="w-67" isPreview={isPreview}>
      <ChartHeader title={EXPENSE_TITLE_BY_MODE[mode]} />
      <ChartContent
        isPreview={showSkeleton}
        isEmpty={isEmpty}
        skeleton={<ExpenseChartSkeleton />}
      >
        <ExpenseChartView data={chartData} />
      </ChartContent>
    </ChartContainer>
  );
};

export default ExpenseChart;
