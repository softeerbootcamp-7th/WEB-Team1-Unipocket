import {
  PERIOD_OPTIONS,
  type PeriodOption,
  type PeriodType,
} from '@/types/period';

// CATEGORY 위젯은 전체, 월별만 허용
export const CATEGORY_PERIOD_OPTIONS: PeriodOption[] = PERIOD_OPTIONS.filter(
  (opt) => opt.type === 'ALL' || opt.type === 'MONTHLY',
);

// PERIOD 위젯은 월별, 주별, 일별만 허용
export type PeriodChartType = Exclude<PeriodType, 'ALL'>;

export const PERIOD_WIDGET_OPTIONS: (PeriodOption & {
  type: PeriodChartType;
})[] = [
  { id: 1, name: '월별', type: 'MONTHLY' },
  { id: 2, name: '주별', type: 'WEEKLY' },
  { id: 3, name: '일별', type: 'DAILY' },
];
