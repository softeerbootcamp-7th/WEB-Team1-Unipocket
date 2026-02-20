import type { WidgetType } from '@/components/chart/widget/type';

import type { CategoryId } from '@/types/category';
import type { CurrencyType } from '@/types/currency';
import type { PeriodType } from '@/types/period';

import type { CountryCode } from '@/data/country/countryCode';

export interface GetWidgetRequest {
  accountBookId: number | string;
  widgetType: WidgetType;
  currencyType?: CurrencyType;
  period?: PeriodType;
}

export interface GetBudgetWidgetResponse {
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

export interface GetPeriodWidgetResponse {
  countryCode: CountryCode;
  itemCount: number;
  items: PeriodWidgetItem[];
}

export interface CategoryWidgetItem {
  category: CategoryId;
  amount: string;
  percent: number;
}

export interface GetCategoryWidgetResponse {
  totalAmount: string;
  countryCode: CountryCode;
  items: CategoryWidgetItem[];
}

export interface GetComparisonWidgetResponse {
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

export interface GetPaymentWidgetResponse {
  paymentMethodCount: number;
  items: PaymentWidgetItem[];
}

export interface CurrencyWidgetItem {
  currencyCode: CountryCode;
  percent: number;
}

export interface GetCurrencyWidgetResponse {
  currencyCount: number;
  items: CurrencyWidgetItem[];
}

export interface WidgetResponseMap {
  BUDGET: GetBudgetWidgetResponse;
  PERIOD: GetPeriodWidgetResponse;
  CATEGORY: GetCategoryWidgetResponse;
  COMPARISON: GetComparisonWidgetResponse;
  PAYMENT: GetPaymentWidgetResponse;
  CURRENCY: GetCurrencyWidgetResponse;
}
