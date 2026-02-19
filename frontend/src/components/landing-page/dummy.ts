import type { Expense } from '@/api/expenses/type';

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
      expenseId: 680,
      accountBookId: 900019,
      travel: {
        id: 1,
        name: '제주도 여행',
        imageKey: 'travel-1.jpg',
      },
      updatedAt: '2026-02-17T10:15:30Z',
      merchantName: '7-Eleven',
      category: 5,
      paymentMethod: {
        isCash: false,
        card: {
          company: 0,
          label: 'My Card 2',
          lastDigits: '0102',
        },
      },
      exchangeRate: 1.0,
      occurredAt: '2026-02-17T10:15:30Z',
      localCurrencyAmount: 14521.0,
      localCurrencyCode: 'KRW',
      baseCurrencyAmount: 14521.0,
      baseCurrencyCode: 'KRW',
      memo: 'seed memo U10-B1-33',
      source: 'MANUAL',
      approvalNumber: 'APP0021033',
      cardNumber: '0033',
      fileLink: null,
    },
    {
      expenseId: 15280,
      accountBookId: 900019,
      travel: {
        id: 1,
        name: '제주도 여행',
        imageKey: 'travel-1.jpg',
      },
      merchantName: 'Yogiyo',
      updatedAt: '2026-02-17T10:15:30Z',
      exchangeRate: 1.0,
      category: 5,
      paymentMethod: {
        isCash: true,
        card: null,
      },
      occurredAt: '2026-02-16T19:43:29Z',
      localCurrencyAmount: 14531.0,
      localCurrencyCode: 'KRW',
      baseCurrencyAmount: 14531.0,
      baseCurrencyCode: 'KRW',
      memo: 'seed memo U10-B1-763',
      source: 'MANUAL',
      approvalNumber: 'APP0021763',
      cardNumber: null,
      fileLink: null,
    },
    {
      expenseId: 7980,
      accountBookId: 900019,
      updatedAt: '2026-02-17T10:15:30Z',
      travel: {
        id: 1,
        name: '제주도 여행',
        imageKey: 'travel-1.jpg',
      },
      merchantName: 'Lotte Mart',
      exchangeRate: 1.0,
      category: 9,
      paymentMethod: {
        isCash: true,
        card: null,
      },
      occurredAt: '2026-02-16T08:48:24Z',
      localCurrencyAmount: 14526.0,
      localCurrencyCode: 'KRW',
      baseCurrencyAmount: 14526.0,
      baseCurrencyCode: 'KRW',
      memo: 'seed memo U10-B1-398',
      source: 'MANUAL',
      approvalNumber: 'APP0021398',
      cardNumber: null,
      fileLink: null,
    },
  ];
}
