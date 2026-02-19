import { useMemo } from 'react';

import {
  type ChartMode,
  EXPENSE_CHART_COLORS,
  EXPENSE_TITLE_BY_MODE,
  type ExpenseChartMode,
} from '@/components/chart/chartType';
import ExpenseChartSkeleton from '@/components/chart/expense/ExpenseChartSkeleton';
import ExpenseChartView from '@/components/chart/expense/ExpenseChartView';
import {
  mockDataForCurrency,
  mockDataForMethod,
} from '@/components/chart/expense/mock';
import ChartContainer from '@/components/chart/layout/ChartContainer';
import ChartContent from '@/components/chart/layout/ChartContent';
import ChartHeader from '@/components/chart/layout/ChartHeader';

import { getCountryInfo } from '@/lib/country';

interface ExpenseChartProps extends ChartMode {
  mode?: ExpenseChartMode;
}

const ExpenseChart = ({
  mode = 'method',
  isPreview = false,
}: ExpenseChartProps) => {
  // 렌더링용 데이터. API 연동 시 변경 필요
  const chartData = useMemo(() => {
    if (mode === 'method') {
      return mockDataForMethod
        .filter((item) => item.percent > 0)
        .map((item, idx) => {
          const color = EXPENSE_CHART_COLORS[idx % EXPENSE_CHART_COLORS.length];

          return {
            id: item.label,
            label: item.label,
            percent: item.percent,
            color,
            subLabel: undefined,
          };
        });
    }

    return mockDataForCurrency
      .filter((item) => item.percent > 0)
      .map((item, idx) => {
        const color = EXPENSE_CHART_COLORS[idx % EXPENSE_CHART_COLORS.length];

        const countryInfo = getCountryInfo(item.countryCode);
        const label = countryInfo?.currencyNameKor || item.countryCode;
        const subLabel = countryInfo
          ? `${countryInfo.currencySign} ${countryInfo.currencyName}`
          : item.countryCode;

        return {
          id: item.countryCode,
          label,
          percent: item.percent,
          color,
          subLabel,
        };
      });
  }, [mode]);
  return (
    <ChartContainer className="w-67" isPreview={isPreview}>
      <ChartHeader title={EXPENSE_TITLE_BY_MODE[mode]} />
      <ChartContent
        isPreview={isPreview || chartData.length === 0}
        skeleton={<ExpenseChartSkeleton />}
      >
        <ExpenseChartView data={chartData} />
      </ChartContent>
    </ChartContainer>
  );
};

export default ExpenseChart;
