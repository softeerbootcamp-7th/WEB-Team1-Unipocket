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
import type { CountryCode } from '@/data/countryCode';
import { useAccountBookStore } from '@/stores/useAccountBookStore';

export const usePeriodChart = (isPreview: boolean) => {
  const [selectedCurrency, setSelectedCurrency] = useState(
    CURRENCY_OPTIONS[0].id,
  );
  const [selectedPeriod, setSelectedPeriod] = useState(
    PERIOD_WIDGET_OPTIONS[0].id,
  );
  const localCountryCode = useAccountBookStore(
    (state) => state.accountBook?.localCountryCode,
  );
  const baseCountryCode = useAccountBookStore(
    (state) => state.accountBook?.baseCountryCode,
  );

  const currencyType: CurrencyType =
    selectedCurrency === CURRENCY_OPTIONS[0].id ? 'BASE' : 'LOCAL';

  const periodType = getPeriodTypeById(selectedPeriod) as PeriodChartType;

  const currentCountryCode: CountryCode =
    (currencyType === 'BASE' ? baseCountryCode : localCountryCode) ?? 'KR';

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
