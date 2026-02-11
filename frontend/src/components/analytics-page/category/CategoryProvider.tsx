import { type ReactNode, useMemo } from 'react';

import type { CurrencyType } from '@/types/currency';

import { CategoryContext } from './CategoryContext';

interface CategoryProviderProps {
  currencyType: CurrencyType;
  children: ReactNode;
}

const CategoryProvider = ({
  currencyType,
  children,
}: CategoryProviderProps) => {
  const value = useMemo(
    () => ({
      currencyType,
    }),
    [currencyType],
  );

  return (
    <CategoryContext.Provider value={value}>
      {children}
    </CategoryContext.Provider>
  );
};

export default CategoryProvider;
