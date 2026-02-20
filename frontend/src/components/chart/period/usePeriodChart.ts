import { useState } from 'react';

import {
  CURRENCY_OPTIONS,
  type PeriodData,
} from '@/components/chart/chartType';
import { MOCK_DATA_MAP } from '@/components/chart/period/mock';
import { parsePeriodItems } from '@/components/chart/period/period.utils';
import {
  PERIOD_WIDGET_OPTIONS,
  type PeriodChartType,
} from '@/components/chart/widgetPeriod';

import type { CurrencyType } from '@/types/currency';
import { getPeriodTypeById } from '@/types/period';

import { useWidgetQuery } from '@/api/widget/query';
import { useAccountBookCountryCode } from '@/stores/accountBook';

export const usePeriodChart = (isPreview: boolean) => {
  const [selectedCurrency, setSelectedCurrency] = useState(
    CURRENCY_OPTIONS[0].id,
  );
  const [selectedPeriod, setSelectedPeriod] = useState(
    PERIOD_WIDGET_OPTIONS[0].id,
  );
  const currencyType: CurrencyType =
    selectedCurrency === CURRENCY_OPTIONS[0].id ? 'BASE' : 'LOCAL';
  const currentCountryCode = useAccountBookCountryCode(currencyType);

  const periodType = getPeriodTypeById(selectedPeriod) as PeriodChartType;

  const { data: apiData, isLoading } = useWidgetQuery('PERIOD', {
    period: periodType,
    currencyType: periodType === 'WEEKLY' ? currencyType : undefined,
  });

  const showSkeleton = isPreview || isLoading || !apiData;
  const chartData: PeriodData[] = showSkeleton
    ? MOCK_DATA_MAP[periodType]
    : parsePeriodItems(apiData.items, periodType);

  return {
    selectedCurrency,
    setSelectedCurrency,
    selectedPeriod,
    setSelectedPeriod,
    periodType,
    currentCountryCode,
    showSkeleton,
    chartData,
  };
};
