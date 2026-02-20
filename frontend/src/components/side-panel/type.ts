import type { CategoryId } from '@/types/category';

import type { Expense } from '@/api/expenses/type';
import type { CurrencyCode } from '@/data/country/currencyCode';

export type SidePanelFormValues = {
  merchantName: string;
  category: CategoryId;
  userCardId?: number;
  occurredAt: Date;
  localCurrencyAmount: number;
  localCurrencyCode: CurrencyCode;
  baseCurrencyAmount: number;
  memo?: string;
  travelId?: number;
};

export type UseSidePanelValuesParams = {
  initialData?: Partial<Expense>;
  selectedDateTime: Date | null;
  onDateTimeClick: () => void;
};
