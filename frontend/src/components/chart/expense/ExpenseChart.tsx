import { useMemo } from 'react';

import {
  type ChartMode,
  EXPENSE_CHART_COLORS,
  EXPENSE_TITLE_BY_MODE,
  type ExpenseChartMode,
} from '@/components/chart/chartType';
import ExpenseChartSkeleton from '@/components/chart/expense/ExpenseChartSkeleton';
import ExpenseChartView from '@/components/chart/expense/ExpenseChartView';
import ChartContainer from '@/components/chart/layout/ChartContainer';
import ChartContent from '@/components/chart/layout/ChartContent';
import ChartHeader from '@/components/chart/layout/ChartHeader';

import { useWidgetQuery } from '@/api/widget/query';
import type {
  CurrencyWidgetResponse,
  PaymentWidgetResponse,
} from '@/api/widget/type';
import { getCountryInfo } from '@/lib/country';

interface ExpenseChartProps extends ChartMode {
  mode?: ExpenseChartMode;
}

const ExpenseChart = ({
  mode = 'method',
  isPreview = false,
}: ExpenseChartProps) => {
  const isMethod = mode === 'method';
  const { data: paymentData, isLoading: isPaymentLoading } =
    useWidgetQuery<PaymentWidgetResponse>('PAYMENT', { enabled: isMethod });
  const { data: currencyData, isLoading: isCurrencyLoading } =
    useWidgetQuery<CurrencyWidgetResponse>('CURRENCY', {
      enabled: !isMethod,
    });

  const isLoading = isMethod ? isPaymentLoading : isCurrencyLoading;

  const chartData = useMemo(() => {
    if (isMethod && paymentData) {
      return paymentData.items
        .filter((item) => item.percent > 0)
        .map((item, idx) => {
          const color = EXPENSE_CHART_COLORS[idx % EXPENSE_CHART_COLORS.length];

          return {
            id: item.name,
            label: item.name,
            percent: item.percent,
            color,
            subLabel: undefined,
          };
        });
    }

    if (!isMethod && currencyData) {
      return currencyData.items
        .filter((item) => item.percent > 0)
        .map((item, idx) => {
          const color = EXPENSE_CHART_COLORS[idx % EXPENSE_CHART_COLORS.length];

          const countryInfo = getCountryInfo(item.currencyCode);
          const label = countryInfo?.currencyNameKor || item.currencyCode;
          const subLabel = countryInfo
            ? `${countryInfo.currencySign} ${countryInfo.currencyName}`
            : item.currencyCode;

          return {
            id: item.currencyCode,
            label,
            percent: item.percent,
            color,
            subLabel,
          };
        });
    }

    return [];
  }, [isMethod, paymentData, currencyData]);

  const showSkeleton = isPreview || isLoading;

  return (
    <ChartContainer className="w-67" isPreview={isPreview}>
      <ChartHeader title={EXPENSE_TITLE_BY_MODE[mode]} />
      <ChartContent
        isPreview={showSkeleton || chartData.length === 0}
        skeleton={<ExpenseChartSkeleton />}
      >
        <ExpenseChartView data={chartData} />
      </ChartContent>
    </ChartContainer>
  );
};

export default ExpenseChart;
