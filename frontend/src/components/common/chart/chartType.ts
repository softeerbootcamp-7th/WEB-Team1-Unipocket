import type { CurrencyType } from '@/types/currency';

// type & interface
export type ExpenseChartMode = 'method' | 'currency';

export interface ExpenseChartUiItem {
  id: string | number; // 리스트 렌더링 key용
  label: string;
  percent: number;
  color: string;
  subLabel?: string;
}

// option
export const CURRENCY_OPTIONS: {
  id: number;
  name: string;
  type: CurrencyType;
}[] = [
  { id: 1, name: '기준 통화', type: 'BASE' },
  { id: 2, name: '현지 통화', type: 'LOCAL' },
];

/** constants */
export const TOTAL_ANIMATION_DURATION = 0.8;

/** colors */
export const CATEGORY_CHART_COLORS = [
  'var(--color-teal-40)',
  'var(--color-teal-50)',
  'var(--color-teal-70)',
  'var(--color-teal-80)',
  'var(--color-teal-95)',
  'var(--color-teal-97)',
  'var(--color-fill-strong)',
];

export const EXPENSE_CHART_COLORS = [
  'var(--color-teal-50)',
  'var(--color-teal-60)',
  'var(--color-teal-70)',
  'var(--color-teal-80)',
  'var(--color-fill-strong)',
];

export const EXPENSE_SUSPENSE_COLOR = [
  'var(--color-fill-strong)',
  'var(--color-fill-normal)',
];
