import { createContext, useContext } from 'react';

import { type CurrencyType } from '@/types/currency';

export interface AnalyticsContextValue {
  currencyType: CurrencyType;
  onCurrencyTypeChange: (currencyType: CurrencyType) => void;
}

export const AnalyticsContext = createContext<AnalyticsContextValue | null>(null);

export const useAnalyticsContext = () => {
  const context = useContext(AnalyticsContext);

  if (!context) {
    throw new Error('AnalyticsContext is missing in AnalyticsProvider tree');
  }

  return context;
};
