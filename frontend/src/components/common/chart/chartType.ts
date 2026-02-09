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

export const EXPENSE_TITLE_BY_MODE: Record<ExpenseChartMode, string> = {
  method: '결제수단별 지출',
  currency: '통화별 지출',
};

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

export const BUDGET_USAGE_RANGES = [
  { max: 30, label: '여유', color: 'var(--color-status-positive)' },
  { max: 60, label: '주의', color: 'var(--color-status-attention)' },
  { max: 80, label: '경고', color: 'var(--color-status-cautionary)' },
  { max: 100, label: '위험', color: 'var(--color-status-negative)' },
] as const;
