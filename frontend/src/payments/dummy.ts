export interface Travel {
  id: number;
  name: string;
}

export interface Budget {
  id: number;
  name: string;
  countryCode: string;
}

export interface Category {
  code: string;
  name: string;
  iconUrl: string;
}

export interface Expense {
  id: number;
  date: string; // ISO 8601 날짜 문자열
  amount: number;
  currency: string;
  krwAmount: number;
  storeName: string;
  memo: string | null; // 메모는 없을 수 있으므로 null 허용
  hasReceipt: boolean;
  travel: Travel;
  budget: Budget;
  category: Category;
}

// getData 함수가 반환하는 타입
export type ExpenseList = Expense[];

export function getData(): ExpenseList {
  return [
    {
      id: 101,
      date: '2024-02-05T18:00:00',
      amount: 1200,
      currency: 'JPY',
      krwAmount: 10800, // 원화 환산 참고값
      storeName: '이치란 라멘',
      memo: '차슈 추가',
      hasReceipt: true, // 영수증 이미지 유무

      travel: {
        id: 10,
        name: '오사카 식도락 여행',
      },
      budget: {
        id: 55,
        name: '오사카 메인 예산',
        countryCode: 'JP',
      },

      category: {
        code: 'FOOD',
        name: '식비',
        iconUrl: 'https://api.app.com/icons/food.png',
      },
    },
    {
      id: 101,
      date: '2024-02-05T18:00:00',
      amount: 1200,
      currency: 'JPY',
      krwAmount: 10800, // 원화 환산 참고값
      storeName: '이치란 라멘',
      memo: '차슈 추가',
      hasReceipt: true, // 영수증 이미지 유무

      travel: {
        id: 10,
        name: '오사카 식도락 여행',
      },
      budget: {
        id: 55,
        name: '오사카 메인 예산',
        countryCode: 'JP',
      },

      category: {
        code: 'FOOD',
        name: '식비',
        iconUrl: 'https://api.app.com/icons/food.png',
      },
    },
    {
      id: 101,
      date: '2024-02-05T18:00:00',
      amount: 1200,
      currency: 'JPY',
      krwAmount: 10800, // 원화 환산 참고값
      storeName: '이치란 라멘',
      memo: '차슈 추가',
      hasReceipt: true, // 영수증 이미지 유무

      travel: {
        id: 10,
        name: '오사카 식도락 여행',
      },
      budget: {
        id: 55,
        name: '오사카 메인 예산',
        countryCode: 'JP',
      },

      category: {
        code: 'FOOD',
        name: '식비',
        iconUrl: 'https://api.app.com/icons/food.png',
      },
    },
    {
      id: 102,
      date: '2024-02-04T12:30:00',
      amount: 500,
      currency: 'JPY',
      krwAmount: 4500,
      storeName: '편의점',
      memo: null,
      hasReceipt: false,

      travel: { id: 10, name: '오사카 식도락 여행' },
      budget: { id: 55, name: '오사카 메인 예산', countryCode: 'JP' },
      category: { code: 'SNACK', name: '간식', iconUrl: '...' },
    },
    {
      id: 102,
      date: '2024-02-04T12:30:00',
      amount: 500,
      currency: 'JPY',
      krwAmount: 4500,
      storeName: '편의점',
      memo: null,
      hasReceipt: false,

      travel: { id: 10, name: '오사카 식도락 여행' },
      budget: { id: 55, name: '오사카 메인 예산', countryCode: 'JP' },
      category: { code: 'SNACK', name: '간식', iconUrl: '...' },
    },
    {
      id: 102,
      date: '2024-02-04T12:30:00',
      amount: 500,
      currency: 'JPY',
      krwAmount: 4500,
      storeName: '편의점',
      memo: null,
      hasReceipt: false,

      travel: { id: 10, name: '오사카 식도락 여행' },
      budget: { id: 55, name: '오사카 메인 예산', countryCode: 'JP' },
      category: { code: 'SNACK', name: '간식', iconUrl: '...' },
    },
  ];
}
