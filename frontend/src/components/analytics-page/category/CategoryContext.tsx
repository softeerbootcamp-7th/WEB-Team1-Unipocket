import { createContext, useContext } from 'react';

import { type CurrencyType } from '@/types/currency';

export interface CategoryContextValue {
  currencyType: CurrencyType;
}

export const CategoryContext = createContext<CategoryContextValue | null>(null);

export const useCategoryContext = () => {
  const context = useContext(CategoryContext);

  if (!context) {
    throw new Error('CategoryContext is missing in ReportCategory tree');
  }

  return context;
};
