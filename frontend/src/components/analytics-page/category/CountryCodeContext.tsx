import { createContext, useContext } from 'react';

import { type CountryCode } from '@/data/countryCode';

export const CountryCodeContext = createContext<CountryCode | null>(null);

export const useCategoryCountryCode = () => {
  const countryCode = useContext(CountryCodeContext);

  if (!countryCode) {
    throw new Error('CountryCodeContext is missing in ReportCategory tree');
  }

  return countryCode;
};
