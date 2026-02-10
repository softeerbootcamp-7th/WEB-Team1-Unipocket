import { type ReactNode } from 'react';

import { type CountryCode } from '@/data/countryCode';

import { CountryCodeContext } from './CountryCodeContext';

interface CountryCodeProviderProps {
  value: CountryCode;
  children: ReactNode;
}

const CountryCodeProvider = ({ value, children }: CountryCodeProviderProps) => {
  return (
    <CountryCodeContext.Provider value={value}>
      {children}
    </CountryCodeContext.Provider>
  );
};

export default CountryCodeProvider;
