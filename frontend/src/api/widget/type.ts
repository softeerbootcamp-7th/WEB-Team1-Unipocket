import type { PeriodType, WidgetType } from '@/components/chart/widget/type';

import type { CategoryType } from '@/types/category';
import type { CurrencyType } from '@/types/currency';

export interface GetWidgetRequest {
  accountBookId: string;
  widgetType: WidgetType;
  currencyType?: CurrencyType;
  period?: PeriodType;
}

export interface BudgetWidgetResponse {
  budget: string;
  baseCountryCode: string;
  localCountryCode: string;
  baseSpentAmount: string;
  localSpentAmount: string;
}

export interface PeriodWidgetItem {
  period: string; // ex: 2026-W04
  amount: string;
}

export interface PeriodWidgetResponse {
  countryCode: string;
  itemCount: number;
  items: PeriodWidgetItem[];
}

export interface CategoryWidgetItem {
  categoryName: CategoryType;
  amount: string;
  percent: number;
}

export interface CategoryWidgetResponse {
  totalAmount: string;
  countryCode: string;
  items: CategoryWidgetItem[];
}

export interface ComparisonWidgetResponse {
  countryCode: string;
  month: number;
  mySpentAmount: string;
  averageSpentAmount: string;
  spentAmountDiff: string;
}

export interface PaymentWidgetItem {
  name: string;
  percent: number;
}

export interface PaymentWidgetResponse {
  paymentMethodCount: number;
  items: PaymentWidgetItem[];
}

export interface CurrencyWidgetItem {
  currencyCode: string;
  percent: number;
}

export interface CurrencyWidgetResponse {
  currencyCount: number;
  items: CurrencyWidgetItem[];
}

export type WidgetResponse =
  | BudgetWidgetResponse
  | PeriodWidgetResponse
  | CategoryWidgetResponse
  | ComparisonWidgetResponse
  | PaymentWidgetResponse
  | CurrencyWidgetResponse;
