import { useState } from 'react';

import {
  type ChartMode,
  CURRENCY_OPTIONS,
  PERIOD_ID,
  PERIOD_OPTIONS,
} from '@/components/chart/chartType';
import ChartContainer from '@/components/chart/layout/ChartContainer';
import ChartContent from '@/components/chart/layout/ChartContent';
import ChartHeader from '@/components/chart/layout/ChartHeader';
import {
  MOCK_DAILY_DATA,
  MOCK_MONTHLY_DATA,
  MOCK_WEEKLY_DATA,
} from '@/components/chart/period/mock';
import PeriodDailyView from '@/components/chart/period/period-view/PeriodDailyView';
import PeriodMonthlyView from '@/components/chart/period/period-view/PeriodMonthlyView';
import PeriodWeeklyView from '@/components/chart/period/period-view/PeriodWeeklyView';
import DropDown from '@/components/common/dropdown/Dropdown';

import { useAccountBookStore } from '@/stores/useAccountBookStore';

const PeriodChart = ({ isPreview = false }: ChartMode) => {
  const [selectedCurrency, setSelectedCurrency] = useState(
    CURRENCY_OPTIONS[0].id,
  );
  const [selectedPeriod, setSelectedPeriod] = useState(PERIOD_OPTIONS[0].id);
  const localCountryCode = useAccountBookStore(
    (state) => state.accountBook?.localCountryCode,
  );
  const baseCountryCode = useAccountBookStore(
    (state) => state.accountBook?.baseCountryCode,
  );

  const isBaseCurrency = selectedCurrency === CURRENCY_OPTIONS[0].id;
  const currentCountryCode = isBaseCurrency
    ? baseCountryCode
    : localCountryCode;

  // TODO: API 연동 시에 스켈레톤은 mockData를 활용하도록 변경 필요
  const renderChart = () => {
    switch (selectedPeriod) {
      case PERIOD_ID.WEEKLY:
        return (
          <PeriodWeeklyView
            data={MOCK_WEEKLY_DATA}
            countryCode={currentCountryCode ?? 'KR'}
            isPreview={isPreview}
          />
        );
      case PERIOD_ID.DAILY:
        return <PeriodDailyView data={MOCK_DAILY_DATA} isPreview={isPreview} />;
      case PERIOD_ID.MONTHLY:
      default:
        return (
          <PeriodMonthlyView data={MOCK_MONTHLY_DATA} isPreview={isPreview} />
        );
    }
  };

  const PADDING_BY_PERIOD: Record<number, string> = {
    [PERIOD_ID.MONTHLY]: 'pt-6 pl-4 pr-5 pb-4',
    [PERIOD_ID.WEEKLY]: 'pt-[30px] px-4 pb-4',
    [PERIOD_ID.DAILY]: 'pt-9 px-5 pb-5',
  };

  return (
    <ChartContainer isPreview={isPreview}>
      <ChartHeader title="기간별 지출">
        {selectedPeriod === PERIOD_ID.WEEKLY && (
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
          options={PERIOD_OPTIONS}
          size="xs"
        />
      </ChartHeader>
      <ChartContent className={PADDING_BY_PERIOD[selectedPeriod]}>
        {renderChart()}
      </ChartContent>
    </ChartContainer>
  );
};

export default PeriodChart;
