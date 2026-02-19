import type { WidgetType } from '@/components/chart/widget/type';

import type { CategoryType } from '@/types/category';
import type { CurrencyType } from '@/types/currency';

import type { CountryCode } from '@/data/countryCode';
import type { PeriodType } from '@/types/period';

export interface GetWidgetRequest {
  accountBookId: string;
  widgetType: WidgetType;
  currencyType?: CurrencyType;
  period?: PeriodType;
}

export interface BudgetWidgetResponse {
  budget: string;
  baseCountryCode: CountryCode;
  localCountryCode: CountryCode;
  baseSpentAmount: string;
  localSpentAmount: string;
}

export interface PeriodWidgetItem {
  period: string; // ex: 2026-W04
  amount: string;
}

export interface PeriodWidgetResponse {
  countryCode: CountryCode;
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
  countryCode: CountryCode;
  items: CategoryWidgetItem[];
}

export interface ComparisonWidgetResponse {
  countryCode: CountryCode;
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
  currencyCode: CountryCode;
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
