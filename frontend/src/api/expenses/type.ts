import type { CategoryId } from '@/types/category';

import type { CardId } from '@/data/card/cardCode';
import type { CurrencyCode } from '@/data/country/currencyCode';

type ExpenseSourceType =
  | 'MANUAL'
  | 'IMAGE_RECEIPT'
  | 'IMAGE_APP_CAPTURE'
  | 'CSV'
  | 'EXCEL';

interface Travel {
  travelId: number;
  name: string;
  imageKey: string | null;
}

// 결제 수단 응답
interface PaymentMethod {
  isCash: boolean;
  card: {
    company: CardId;
    label: string;
    lastDigits: string;
  } | null;
}

// @TODO 모든 amount는 string으로 바꿔야함
interface Expense {
  expenseId: number;
  accountBookId: number;
  travel: Travel | null;
  merchantName: string;
  exchangeRate: number;
  category: CategoryId;
  paymentMethod: PaymentMethod;
  occurredAt: string;
  updatedAt: string;
  localCurrencyAmount: number;
  localCurrencyCode: CurrencyCode;
  baseCurrencyAmount: number;
  baseCurrencyCode: CurrencyCode;
  memo: string | null;
  source: ExpenseSourceType;
  approvalNumber: string | null;
  cardNumber: string | null;
  fileLink: string | null;
}

type ExpenseResponseBase = Pick<
  Expense,
  | 'expenseId'
  | 'accountBookId'
  | 'merchantName'
  | 'category'
  | 'paymentMethod'
  | 'localCurrencyAmount'
  | 'localCurrencyCode'
  | 'baseCurrencyAmount'
  | 'baseCurrencyCode'
  | 'occurredAt'
>;

type GetExpenseDetailResponse = Required<Expense>;

type UpdateExpenseRequest = Partial<
  Pick<
    Expense,
    | 'merchantName'
    | 'category'
    | 'occurredAt'
    | 'localCurrencyAmount'
    | 'localCurrencyCode'
    | 'baseCurrencyAmount'
    | 'memo'
  > & { userCardId: number; travelId: number }
>;

type UpdateExpenseResponse = Required<
  Omit<Expense, 'travel' | 'updatedAt' | 'exchangeRate'> & {
    travelId: number | null;
    displayMerchantName: string;
  }
>;

type CreateManualExpenseRequest = Required<
  Pick<
    Expense,
    | 'merchantName'
    | 'category'
    | 'occurredAt'
    | 'localCurrencyAmount'
    | 'localCurrencyCode'
    | 'baseCurrencyAmount'
  >
> & {
  memo?: string;
  userCardId?: number; // 안 보낼 경우 cash로 처리
  travelId?: number;
};

interface GetExpensesResponse {
  expenses: Expense[];
  totalCount: number;
  page: number;
  size: number;
}

type CreateManualExpenseResponse = Required<ExpenseResponseBase>;

type ExpenseSearchFilter = Partial<
  Pick<Expense, 'category' | 'merchantName'> & {
    startDate: string;
    endDate: string;
    minAmount: number;
    maxAmount: number;
    travelId: number;
    page: number;
    size: number;
    sort: string[];
  }
>;

type GetExpenseFileUrlResponse = {
  presignedUrl: string;
  expiredsInSeconds: number;
};

// 거래처명 검색 응답
interface SearchMerchantNamesResponse {
  merchantNames: string[];
}

type BulkUpdateExpenseItem = { expenseId: number } & Partial<
  Pick<
    Expense,
    | 'merchantName'
    | 'category'
    | 'occurredAt'
    | 'localCurrencyAmount'
    | 'localCurrencyCode'
    | 'baseCurrencyAmount'
    | 'memo'
  > & { userCardId: number; travelId: number }
>;

interface BulkUpdateExpenseRequest {
  items: BulkUpdateExpenseItem[];
}

interface BulkUpdateExpenseResponse {
  totalUpdated: number;
  items: UpdateExpenseResponse[];
}

export type {
  BulkUpdateExpenseItem,
  BulkUpdateExpenseRequest,
  BulkUpdateExpenseResponse,
  CreateManualExpenseRequest,
  CreateManualExpenseResponse,
  Expense,
  ExpenseSearchFilter,
  GetExpenseDetailResponse,
  GetExpenseFileUrlResponse,
  GetExpensesResponse,
  PaymentMethod,
  SearchMerchantNamesResponse,
  UpdateExpenseRequest,
  UpdateExpenseResponse,
};
