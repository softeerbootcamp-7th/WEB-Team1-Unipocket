import { type CategoryId } from '@/types/category';

import { type CountryCode } from '@/data/countryCode';

export interface CreateAccountBookRequest {
  localCountryCode: CountryCode;
  startDate: string;
  endDate: string;
}

export interface AccountBookSummary {
  id: number;
  title: string;
  isMain: boolean;
}

export interface AccountBookDetail {
  id: number;
  title: string;
  localCountryCode: CountryCode;
  baseCountryCode: CountryCode;
  budget: number | null;
  startDate: string;
  endDate: string;
  isMain?: boolean;
}

export interface UpdateAccountBookRequest {
  title?: string;
  localCountryCode?: CountryCode;
  baseCountryCode?: CountryCode;
  budget?: number | null;
  startDate?: string;
  endDate?: string;
}

export interface AccountBookResponse {
  id: number;
  title: string;
  localCountryCode: CountryCode;
  baseCountryCode: CountryCode;
  budget: number | null;
  startDate: string;
  endDate: string;
  isMain?: boolean;
}

interface AnalysisChartItem {
  date: string;
  cumulatedAmount: string;
}

interface AnalysisCategoryItem {
  categoryIndex: CategoryId;
  mySpentAmount: string;
  averageSpentAmount: string;
}

export interface AnalysisResponse {
  countryCode: string;
  compareWithAverage: {
    month: number;
    mySpentAmount: string;
    averageSpentAmount: string;
    spentAmountDiff: string;
  };
  compareWithLastMonth: {
    diff: string;
    thisMonth: string;
    thisMonthCount: number;
    lastMonth: string;
    lastMonthCount: number;
    totalSpent: {
      thisMonthToDate: string;
      lastMonthTotal: string;
    };
    thisMonthSpent: string;
    thisMonthItem: AnalysisChartItem[];
    prevMonthItem: AnalysisChartItem[];
  };
  compareByCategory: {
    maxDiffCategoryIndex: CategoryId;
    isOverSpent: boolean;
    maxLabel: string;
    items: AnalysisCategoryItem[];
  };
}
