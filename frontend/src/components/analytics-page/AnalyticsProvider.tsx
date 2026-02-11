import { type ReactNode } from 'react';

import type { CurrencyType } from '@/types/currency';

import { AnalyticsContext } from './AnalyticsContext';

interface AnalyticsProviderProps {
  currencyType: CurrencyType;
  onCurrencyTypeChange: (currencyType: CurrencyType) => void;
  children: ReactNode;
}

const AnalyticsProvider = ({
  currencyType,
  onCurrencyTypeChange,
  children,
}: AnalyticsProviderProps) => {
  return (
    <AnalyticsContext.Provider value={{ currencyType, onCurrencyTypeChange }}>
      {children}
    </AnalyticsContext.Provider>
  );
};

export default AnalyticsProvider;
