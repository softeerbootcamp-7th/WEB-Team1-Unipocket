import { type ChartMode, CURRENCY_OPTIONS } from '@/components/chart/chartType';
import ChartContainer from '@/components/chart/layout/ChartContainer';
import ChartContent from '@/components/chart/layout/ChartContent';
import ChartHeader from '@/components/chart/layout/ChartHeader';
import { CHART_MESSAGES } from '@/components/chart/message';
import PeriodDailyView from '@/components/chart/period/period-view/PeriodDailyView';
import PeriodMonthlyView from '@/components/chart/period/period-view/PeriodMonthlyView';
import PeriodWeeklyView from '@/components/chart/period/period-view/PeriodWeeklyView';
import { usePeriodChart } from '@/components/chart/period/usePeriodChart';
import {
  PERIOD_WIDGET_OPTIONS,
  type PeriodChartType,
} from '@/components/chart/widgetPeriod';
import DropDown from '@/components/common/dropdown/Dropdown';

const PADDING_BY_PERIOD: Record<PeriodChartType, string> = {
  MONTHLY: 'pt-6 pl-4 pr-5 pb-4',
  WEEKLY: 'pt-[30px] px-4 pb-4',
  DAILY: 'pt-9 px-5 pb-5',
};

const PERIOD_VIEW_MAP = {
  MONTHLY: PeriodMonthlyView,
  WEEKLY: PeriodWeeklyView,
  DAILY: PeriodDailyView,
};

const PeriodChart = ({ isPreview = false }: ChartMode) => {
  const {
    selectedCurrency,
    setSelectedCurrency,
    selectedPeriod,
    setSelectedPeriod,
    periodType,
    currentCountryCode,
    showSkeleton,
    isEmpty,
    chartData,
  } = usePeriodChart(isPreview);

  const PeriodView = PERIOD_VIEW_MAP[periodType];

  return (
    <ChartContainer isPreview={isPreview}>
      <ChartHeader title="기간별 지출">
        {periodType === 'WEEKLY' && (
          <DropDown
            selectedId={selectedCurrency}
            onSelect={setSelectedCurrency}
            options={CURRENCY_OPTIONS}
            size="xs"
          />
        )}
        <DropDown
          selectedId={selectedPeriod}
          onSelect={setSelectedPeriod}
          options={PERIOD_WIDGET_OPTIONS}
          size="xs"
        />
      </ChartHeader>
      <ChartContent
        className={PADDING_BY_PERIOD[periodType]}
        isEmpty={isEmpty}
        emptyMessage={CHART_MESSAGES.PERIOD_EMPTY}
        isPreview={showSkeleton}
        skeleton={
          <PeriodView
            data={chartData}
            countryCode={currentCountryCode}
            isPreview={true}
          />
        }
      >
        <PeriodView data={chartData} countryCode={currentCountryCode} />
      </ChartContent>
    </ChartContainer>
  );
};

export default PeriodChart;
