import { useState } from 'react';

import {
  type ChartMode,
  CURRENCY_OPTIONS,
  type PeriodData,
} from '@/components/chart/chartType';
import ChartContainer from '@/components/chart/layout/ChartContainer';
import ChartContent from '@/components/chart/layout/ChartContent';
import ChartHeader from '@/components/chart/layout/ChartHeader';
import {
  MOCK_DAILY_DATA,
  MOCK_MONTHLY_DATA,
  MOCK_WEEKLY_DATA,
} from '@/components/chart/period/mock';
import { parsePeriodItems } from '@/components/chart/period/period.utils';
import PeriodDailyView from '@/components/chart/period/period-view/PeriodDailyView';
import PeriodMonthlyView from '@/components/chart/period/period-view/PeriodMonthlyView';
import PeriodWeeklyView from '@/components/chart/period/period-view/PeriodWeeklyView';
import {
  PERIOD_WIDGET_OPTIONS,
  type PeriodChartType,
} from '@/components/chart/widgetPeriod';
import DropDown from '@/components/common/dropdown/Dropdown';

import type { CurrencyType } from '@/types/currency';

import { useWidgetQuery } from '@/api/widget/query';
import type { PeriodWidgetResponse } from '@/api/widget/type';
import type { CountryCode } from '@/data/countryCode';
import { useAccountBookStore } from '@/stores/useAccountBookStore';

const MOCK_DATA_MAP: Record<PeriodChartType, PeriodData[]> = {
  MONTHLY: MOCK_MONTHLY_DATA,
  WEEKLY: MOCK_WEEKLY_DATA,
  DAILY: MOCK_DAILY_DATA,
};

const PADDING_BY_PERIOD: Record<PeriodChartType, string> = {
  MONTHLY: 'pt-6 pl-4 pr-5 pb-4',
  WEEKLY: 'pt-[30px] px-4 pb-4',
  DAILY: 'pt-9 px-5 pb-5',
};

const PeriodChart = ({ isPreview = false }: ChartMode) => {
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

  const periodOption = PERIOD_WIDGET_OPTIONS.find(
    (opt) => opt.id === selectedPeriod,
  )!;

  const periodType = periodOption.type;

  const currentCountryCode: CountryCode =
    (currencyType === 'BASE' ? baseCountryCode : localCountryCode) ?? 'KR';

  const { data: apiData, isLoading } = useWidgetQuery<PeriodWidgetResponse>(
    'PERIOD',
    {
      period: periodType,
      currencyType: periodType === 'WEEKLY' ? currencyType : undefined,
    },
  );

  const showSkeleton = isPreview || isLoading || !apiData;
  const chartData = showSkeleton
    ? MOCK_DATA_MAP[periodType]
    : parsePeriodItems(apiData.items, periodType);

  const renderChart = () => {
    switch (periodType) {
      case 'WEEKLY':
        return (
          <PeriodWeeklyView
            data={chartData}
            countryCode={currentCountryCode}
            isPreview={showSkeleton}
          />
        );
      case 'DAILY':
        return <PeriodDailyView data={chartData} isPreview={showSkeleton} />;
      case 'MONTHLY':
      default:
        return <PeriodMonthlyView data={chartData} isPreview={showSkeleton} />;
    }
  };

  return (
    <ChartContainer isPreview={isPreview}>
      <ChartHeader title="기간별 지출">
        {periodType === 'WEEKLY' && (
          <DropDown
            selected={selectedCurrency}
            onSelect={setSelectedCurrency}
            options={CURRENCY_OPTIONS}
            size="xs"
          />
        )}
        <DropDown
          selected={selectedPeriod}
          onSelect={setSelectedPeriod}
          options={PERIOD_WIDGET_OPTIONS}
          size="xs"
        />
      </ChartHeader>
      <ChartContent className={PADDING_BY_PERIOD[periodType]}>
        {renderChart()}
      </ChartContent>
    </ChartContainer>
  );
};

export default PeriodChart;
