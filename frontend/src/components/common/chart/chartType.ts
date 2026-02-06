import type { CurrencyType } from '@/types/currency';

export const CATEGORY_COLORS = [
  'var(--color-teal-40)',
  'var(--color-teal-50)',
  'var(--color-teal-70)',
  'var(--color-teal-80)',
  'var(--color-teal-95)',
  'var(--color-teal-97)',
  'var(--color-fill-strong)',
];

export const CURRENCY_OPTIONS: {
  id: number;
  name: string;
  type: CurrencyType;
}[] = [
  { id: 1, name: '기준 통화', type: 'BASE' },
  { id: 2, name: '현지 통화', type: 'LOCAL' },
];
