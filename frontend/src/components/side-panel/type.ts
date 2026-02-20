import type { CategoryId } from '@/types/category';

import type { CurrencyCode } from '@/data/country/currencyCode';

export interface SidePanelFormValues {
  merchantName: string;
  category: CategoryId;
  userCardId?: number;
  occurredAt: Date;
  localCurrencyAmount: number;
  localCurrencyCode: CurrencyCode;
  baseCurrencyAmount: number;
  memo?: string;
  travelId?: number;
}
