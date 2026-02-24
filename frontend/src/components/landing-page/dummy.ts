import type { Expense } from '@/api/expenses/type';
import type { TempExpense } from '@/api/temporary-expenses/type';

// export interface Travel {
//   id: number;
//   name: string;
//   imageUrl: string;
// }

// export interface Budget {
//   id: number;
//   name: string;
//   countryCode: string;
// }

// interface Company {
//   name: string;
//   iconUrl: string;
// }

// interface Card {
//   company: Company;
//   label: string;
//   lastDigits: string;
// }

// export interface PaymentMethod {
//   isCash: boolean;
//   card: Card | null;
// }

// export interface Expense {
//   expenseId: number;
//   occurredAt: string; // ISO 8601 형식의 날짜 문자열
//   merchantName: string; // 거래처
//   categoryCode: CategoryType; // 카테고리
//   localCurrencyCode: string; // 현지통화
//   localCurrencyAmount: number; // 현지금액
//   standardCurrency: string; // 기준통화
//   standardAmount: number; // 기준금액
//   exchangeRate: number; // 환율
//   paymentMethod: PaymentMethod; // 결제수단
//   travel: Travel;
//   memo: string | null;
//   file: string;
// }

export function getData(): Expense[] {
  return [
    {
      expenseId: 415,
      accountBookId: 900019,
      travel: {
        travelId: 1,
        name: '미국 여행',
        imageKey: 'travel-usa.jpg',
      },
      updatedAt: '2026-02-24T01:11:56.707626',
      merchantName: 'Walmart',
      category: 6,
      paymentMethod: {
        isCash: false,
        card: {
          company: 0,
          label: 'My Card',
          lastDigits: '0000',
          userCardId: 1,
        },
      },
      exchangeRate: 1118.59,
      occurredAt: '2021-08-12T11:36:18',
      localCurrencyAmount: 11.72,
      localCurrencyCode: 'USD',
      baseCurrencyAmount: 13109.87,
      baseCurrencyCode: 'KRW',
      memo: null,
      source: 'IMAGE_RECEIPT',
      approvalNumber: 'TMP0415',
      cardNumber: null,
      fileLink: null,
    },
    {
      expenseId: 416,
      accountBookId: 900019,
      travel: {
        travelId: 1,
        name: '미국 여행',
        imageKey: 'travel-usa.jpg',
      },
      updatedAt: '2026-02-24T01:11:56.707626',
      merchantName: 'Walmart',
      category: 6,
      paymentMethod: {
        isCash: false,
        card: {
          company: 0,
          label: 'My Card',
          lastDigits: '0000',
          userCardId: 1,
        },
      },
      exchangeRate: 1118.59,
      occurredAt: '2021-08-12T11:36:18',
      localCurrencyAmount: 2.96,
      localCurrencyCode: 'USD',
      baseCurrencyAmount: 3311.03,
      baseCurrencyCode: 'KRW',
      memo: null,
      source: 'IMAGE_RECEIPT',
      approvalNumber: 'TMP0416',
      cardNumber: null,
      fileLink: null,
    },
    {
      expenseId: 417,
      accountBookId: 900019,
      travel: {
        travelId: 1,
        name: '미국 여행',
        imageKey: 'travel-usa.jpg',
      },
      updatedAt: '2026-02-24T01:11:56.707626',
      merchantName: 'Walmart',
      category: 6,
      paymentMethod: {
        isCash: false,
        card: {
          company: 0,
          label: 'My Card',
          lastDigits: '0000',
          userCardId: 1,
        },
      },
      exchangeRate: 1118.59,
      occurredAt: '2021-08-12T11:36:18',
      localCurrencyAmount: 3.98,
      localCurrencyCode: 'USD',
      baseCurrencyAmount: 4451.99,
      baseCurrencyCode: 'KRW',
      memo: null,
      source: 'IMAGE_RECEIPT',
      approvalNumber: 'TMP0417',
      cardNumber: null,
      fileLink: null,
    },
    {
      expenseId: 418,
      accountBookId: 900019,
      travel: {
        travelId: 1,
        name: '미국 여행',
        imageKey: 'travel-usa.jpg',
      },
      updatedAt: '2026-02-24T01:11:56.707626',
      merchantName: 'Walmart',
      category: 6,
      paymentMethod: {
        isCash: false,
        card: {
          company: 0,
          label: 'My Card',
          lastDigits: '0000',
          userCardId: 1,
        },
      },
      exchangeRate: 1118.59,
      occurredAt: '2021-08-12T11:36:18',
      localCurrencyAmount: 2.44,
      localCurrencyCode: 'USD',
      baseCurrencyAmount: 2729.36,
      baseCurrencyCode: 'KRW',
      memo: null,
      source: 'IMAGE_RECEIPT',
      approvalNumber: 'TMP0418',
      cardNumber: null,
      fileLink: null,
    },
    {
      expenseId: 419,
      accountBookId: 900019,
      travel: {
        travelId: 1,
        name: '미국 여행',
        imageKey: 'travel-usa.jpg',
      },
      updatedAt: '2026-02-24T01:11:56.707626',
      merchantName: 'Walmart',
      category: 6,
      paymentMethod: {
        isCash: false,
        card: {
          company: 0,
          label: 'My Card',
          lastDigits: '0000',
          userCardId: 1,
        },
      },
      exchangeRate: 1118.59,
      occurredAt: '2021-08-12T11:36:18',
      localCurrencyAmount: 11.72,
      localCurrencyCode: 'USD',
      baseCurrencyAmount: 13109.87,
      baseCurrencyCode: 'KRW',
      memo: null,
      source: 'IMAGE_RECEIPT',
      approvalNumber: 'TMP0419',
      cardNumber: null,
      fileLink: null,
    },
  ];
}

export const TEMP_EXPENSE_DUMMY: TempExpense[] = [
  {
    tempExpenseId: 415,
    tempExpenseMetaId: 95,
    fileId: 158,
    merchantName: 'Walmart',
    category: 6,
    localCountryCode: 'USD',
    localCurrencyAmount: 11.72,
    baseCountryCode: 'KRW',
    baseCurrencyAmount: 13109.87,
    memo: null,
    occurredAt: '2021-08-12T11:36:18',
    status: 'NORMAL',
    cardLastFourDigits: '',
  },
  {
    tempExpenseId: 416,
    tempExpenseMetaId: 95,
    fileId: 158,
    merchantName: 'Walmart',
    category: 6,
    localCountryCode: 'USD',
    localCurrencyAmount: 2.96,
    baseCountryCode: 'KRW',
    baseCurrencyAmount: 3311.03,
    memo: null,
    occurredAt: '2021-08-12T11:36:18',
    status: 'NORMAL',
    cardLastFourDigits: '',
  },
  {
    tempExpenseId: 417,
    tempExpenseMetaId: 95,
    fileId: 158,
    merchantName: 'Walmart',
    category: 6,
    localCountryCode: 'USD',
    localCurrencyAmount: 3.98,
    baseCountryCode: 'KRW',
    baseCurrencyAmount: 4451.99,
    memo: null,
    occurredAt: '2021-08-12T11:36:18',
    status: 'NORMAL',
    cardLastFourDigits: '',
  },
  {
    tempExpenseId: 418,
    tempExpenseMetaId: 95,
    fileId: 158,
    merchantName: 'Walmart',
    category: 6,
    localCountryCode: 'USD',
    localCurrencyAmount: 2.44,
    baseCountryCode: 'KRW',
    baseCurrencyAmount: 2729.36,
    memo: null,
    occurredAt: '2021-08-12T11:36:18',
    status: 'NORMAL',
    cardLastFourDigits: '',
  },
  {
    tempExpenseId: 419,
    tempExpenseMetaId: 95,
    fileId: 158,
    merchantName: 'Walmart',
    category: 6,
    localCountryCode: 'USD',
    localCurrencyAmount: 11.72,
    baseCountryCode: 'KRW',
    baseCurrencyAmount: 13109.87,
    memo: null,
    occurredAt: '2021-08-12T11:36:18',
    status: 'NORMAL',
    cardLastFourDigits: '',
  },
];
