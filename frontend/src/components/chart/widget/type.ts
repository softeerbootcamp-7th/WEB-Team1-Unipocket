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

export interface WidgetItem {
  order: number;
  widgetType: WidgetKind;
  currencyType?: 'BASE' | 'LOCAL';
  period?: 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'ALL';
}
