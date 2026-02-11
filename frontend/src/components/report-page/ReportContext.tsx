import { createContext, useContext } from 'react';

import { type CurrencyType } from '@/types/currency';

export interface ReportContextValue {
  currencyType: CurrencyType;
  onCurrencyTypeChange: (currencyType: CurrencyType) => void;
}

export const ReportContext = createContext<ReportContextValue | null>(null);

export const useReportContext = () => {
  const context = useContext(ReportContext);

  if (!context) {
    throw new Error('ReportContext is missing in ReportProvider tree');
  }

  return context;
};
