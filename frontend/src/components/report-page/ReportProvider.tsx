import { type ReactNode } from 'react';

import type { CurrencyType } from '@/types/currency';

import { ReportContext } from './ReportContext';

interface ReportProviderProps {
  currencyType: CurrencyType;
  onCurrencyTypeChange: (currencyType: CurrencyType) => void;
  children: ReactNode;
}

const ReportProvider = ({
  currencyType,
  onCurrencyTypeChange,
  children,
}: ReportProviderProps) => {
  return (
    <ReportContext.Provider value={{ currencyType, onCurrencyTypeChange }}>
      {children}
    </ReportContext.Provider>
  );
};

export default ReportProvider;