export const WIDGET_TYPES = [
  'BUDGET',
  'CATEGORY',
  'COMPARISON',
  'PAYMENT',
  'CURRENCY',
  'PERIOD',
] as const;

export type WidgetType = (typeof WIDGET_TYPES)[number];

export type WidgetKind = WidgetType | 'BLANK';

export const PERIOD_TYPES = ['DAILY', 'WEEKLY', 'MONTHLY', 'ALL'] as const;

export type PeriodType = (typeof PERIOD_TYPES)[number];

export interface WidgetItem {
  order: number;
  widgetType: WidgetKind;
  currencyType?: 'BASE' | 'LOCAL';
  period?: PeriodType;
}
