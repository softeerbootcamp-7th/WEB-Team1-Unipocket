import { useMemo, useState } from 'react';

const ERROR_MESSAGES = {
  INVALID_NUMBER: '숫자만 입력할 수 있어요.',
  ZERO_OR_NEGATIVE: '0보다 큰 금액을 입력해주세요.',
} as const;

const isValidNumberFormat = (value: string): boolean => {
  const sanitized = value.replace(/[^0-9.]/g, '');
  return value === sanitized && (sanitized.match(/\./g)?.length ?? 0) <= 1;
};

const sanitizeInput = (value: string): string => value.replace(/[^0-9.]/g, '');

const formatBaseCurrency = (amount: number): string =>
  Number(amount.toFixed(0)).toLocaleString();

const formatLocalCurrency = (amount: number): string =>
  Number(amount.toFixed(2)).toLocaleString(undefined, {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });

type Direction = 'toBase' | 'toLocal';

const convertCurrency = (
  num: number,
  direction: Direction,
  rate: number,
): string =>
  direction === 'toBase'
    ? formatBaseCurrency(num * rate)
    : formatLocalCurrency(num / rate);

const useCurrencyConverter = (rate: number) => {
  const [localCurrency, setLocalCurrency] = useState('');
  const [baseCurrency, setBaseCurrency] = useState('');
  const [amountError, setAmountError] = useState<string | null>(null);

  const isValid = useMemo(() => {
    return localCurrency !== '' && baseCurrency !== '' && !amountError;
  }, [localCurrency, baseCurrency, amountError]);

  const handleCurrencyChange = (value: string, direction: Direction) => {
    if (!isValidNumberFormat(value)) {
      setAmountError(ERROR_MESSAGES.INVALID_NUMBER);
      return;
    }

    const sanitized = sanitizeInput(value);
    const num = Number(sanitized);
    const [setPrimary, setSecondary] =
      direction === 'toBase'
        ? [setLocalCurrency, setBaseCurrency]
        : [setBaseCurrency, setLocalCurrency];

    setPrimary(sanitized);

    if (sanitized !== '' && num <= 0) {
      setAmountError(ERROR_MESSAGES.ZERO_OR_NEGATIVE);
      setSecondary('');
      return;
    }

    setAmountError(null);
    setSecondary(sanitized === '' ? '' : convertCurrency(num, direction, rate));
  };

  return {
    localCurrency,
    baseCurrency,
    amountError,
    handleCurrencyChange,
    isValid,
  };
};

export default useCurrencyConverter;
