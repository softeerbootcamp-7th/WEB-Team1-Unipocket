import {
  EXPENSE_CHART_COLORS,
  type ExpenseChartUiItem,
} from '@/components/chart/chartType';

import type {
  CurrencyWidgetResponse,
  PaymentWidgetResponse,
} from '@/api/widget/type';
import { getCountryInfo } from '@/lib/country';

export function transformPaymentChartData(
  paymentData?: PaymentWidgetResponse,
): ExpenseChartUiItem[] {
  if (!paymentData) return [];

  return paymentData.items
    .filter((item) => item.percent > 0)
    .map((item, idx) => ({
      id: item.name,
      label: item.name,
      percent: item.percent,
      color: EXPENSE_CHART_COLORS[idx % EXPENSE_CHART_COLORS.length],
    }));
}

export function transformCurrencyChartData(
  currencyData?: CurrencyWidgetResponse,
): ExpenseChartUiItem[] {
  if (!currencyData) return [];

  return currencyData.items
    .filter((item) => item.percent > 0)
    .map((item, idx) => {
      const color = EXPENSE_CHART_COLORS[idx % EXPENSE_CHART_COLORS.length];

      const countryInfo = getCountryInfo(item.currencyCode);

      return {
        id: item.currencyCode,
        label: countryInfo?.currencyNameKor || item.currencyCode,
        percent: item.percent,
        color,
        subLabel: countryInfo
          ? `${countryInfo.currencySign} ${countryInfo.currencyName}`
          : item.currencyCode,
      };
    });
}
