import type { CategoryType } from '@/types/category';

export interface Travel {
  id: number;
  name: string;
  imageUrl: string;
}

export interface Budget {
  id: number;
  name: string;
  countryCode: string;
}

interface Company {
  name: string;
  iconUrl: string;
}

interface Card {
  company: Company;
  label: string;
  lastDigits: string;
}

export interface PaymentMethod {
  isCash: boolean;
  card: Card | null;
}

export interface Expense {
  expenseId: number;
  date: string; // ISO 8601 형식의 날짜 문자열
  merchantName: string; // 거래처
  categoryCode: CategoryType; // 카테고리
  localCurrency: string; // 현지통화
  localAmount: number; // 현지금액
  standardCurrency: string; // 기준통화
  standardAmount: number; // 기준금액
  exchangeRate: number; // 환율
  paymentMethod: PaymentMethod; // 결제수단
  travel: Travel;
  memo: string | null;
  file: string;
}

// getData 함수가 반환하는 타입
export type ExpenseList = Expense[];

export function getData(): ExpenseList {
  return [
    // --- 2024년 2월 10일 ---
    {
      expenseId: 200,
      date: '2024-02-10T19:00:00',
      merchantName: 'Shake Shack NYC',
      categoryCode: '식비',
      localCurrency: 'USD',
      localAmount: 24.5,
      standardCurrency: 'KRW',
      standardAmount: 32830,
      exchangeRate: 1340,
      paymentMethod: {
        isCash: false,
        card: {
          company: {
            name: 'Visa',
            iconUrl:
              'https://upload.wikimedia.org/wikipedia/commons/d/d6/Visa_2021.svg',
          },
          label: '트래블월렛',
          lastDigits: '1122',
        },
      },
      travel: {
        id: 1,
        name: '뉴욕 교환학생',
        imageUrl:
          'https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?q=80&w=400',
      },
      memo: '저녁 식사',
      file: 'https://api.app.com/receipts/200.png',
    },
    {
      expenseId: 199,
      date: '2024-02-10T14:30:00',
      merchantName: 'Apple Store Fifth Ave',
      categoryCode: '쇼핑',
      localCurrency: 'USD',
      localAmount: 129,
      standardCurrency: 'KRW',
      standardAmount: 172860,
      exchangeRate: 1340,
      paymentMethod: {
        isCash: false,
        card: {
          company: {
            name: 'Mastercard',
            iconUrl:
              'https://upload.wikimedia.org/wikipedia/commons/2/2a/Mastercard-logo.svg',
          },
          label: '현대 제로',
          lastDigits: '4567',
        },
      },
      travel: {
        id: 1,
        name: '뉴욕 교환학생',
        imageUrl:
          'https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?q=80&w=400',
      },
      memo: '에어팟 교체',
      file: 'https://api.app.com/receipts/199.png',
    },

    // --- 2024년 2월 09일 ---
    {
      expenseId: 198,
      date: '2024-02-09T20:00:00',
      merchantName: 'Yakiniku Itcho Osaka',
      categoryCode: '식비',
      localCurrency: 'JPY',
      localAmount: 8500,
      standardCurrency: 'KRW',
      standardAmount: 76500,
      exchangeRate: 9.0,
      paymentMethod: {
        isCash: false,
        card: {
          company: {
            name: 'Hana Card',
            iconUrl: 'https://www.hanacard.co.kr/images/common/logo.png',
          },
          label: '트래블로그',
          lastDigits: '9900',
        },
      },
      travel: {
        id: 2,
        name: '오사카 미식 여행',
        imageUrl:
          'https://images.unsplash.com/photo-1590559899731-a382839e5549?q=80&w=400',
      },
      memo: '와규 코스',
      file: 'https://api.app.com/receipts/198.png',
    },
    {
      expenseId: 197,
      date: '2024-02-09T13:00:00',
      merchantName: 'FamilyMart',
      categoryCode: '식비',
      localCurrency: 'JPY',
      localAmount: 650,
      standardCurrency: 'KRW',
      standardAmount: 5850,
      exchangeRate: 9.0,
      paymentMethod: { isCash: true, card: null },
      travel: {
        id: 2,
        name: '오사카 미식 여행',
        imageUrl:
          'https://images.unsplash.com/photo-1590559899731-a382839e5549?q=80&w=400',
      },
      memo: '삼각김밥과 푸딩',
      file: '',
    },

    // --- 2024년 2월 08일 ---
    {
      expenseId: 196,
      date: '2024-02-08T21:30:00',
      merchantName: 'Paris Airbnb',
      categoryCode: '거주',
      localCurrency: 'EUR',
      localAmount: 120,
      standardCurrency: 'KRW',
      standardAmount: 174000,
      exchangeRate: 1450,
      paymentMethod: {
        isCash: false,
        card: {
          company: {
            name: 'Mastercard',
            iconUrl:
              'https://upload.wikimedia.org/wikipedia/commons/2/2a/Mastercard-logo.svg',
          },
          label: '국민 노리2',
          lastDigits: '7788',
        },
      },
      travel: {
        id: 3,
        name: '유럽 배낭여행',
        imageUrl:
          'https://images.unsplash.com/photo-1502602898657-3e91760cbb34?q=80&w=400',
      },
      memo: '파리 숙소 1박',
      file: 'https://api.app.com/receipts/196.png',
    },
    {
      expenseId: 195,
      date: '2024-02-08T10:00:00',
      merchantName: 'Metro de Paris',
      categoryCode: '교통비',
      localCurrency: 'EUR',
      localAmount: 2.1,
      standardCurrency: 'KRW',
      standardAmount: 3045,
      exchangeRate: 1450,
      paymentMethod: { isCash: true, card: null },
      travel: {
        id: 3,
        name: '유럽 배낭여행',
        imageUrl:
          'https://images.unsplash.com/photo-1502602898657-3e91760cbb34?q=80&w=400',
      },
      memo: '지하철 티켓',
      file: '',
    },

    // --- 2024년 2월 07일 ---
    {
      expenseId: 194,
      date: '2024-02-07T18:00:00',
      merchantName: 'London Pub',
      categoryCode: '여가',
      localCurrency: 'GBP',
      localAmount: 15,
      standardCurrency: 'KRW',
      standardAmount: 25500,
      exchangeRate: 1700,
      paymentMethod: {
        isCash: false,
        card: {
          company: {
            name: 'Visa',
            iconUrl:
              'https://upload.wikimedia.org/wikipedia/commons/d/d6/Visa_2021.svg',
          },
          label: '트래블로그',
          lastDigits: '5566',
        },
      },
      travel: {
        id: 4,
        name: '영국 어학연수',
        imageUrl:
          'https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?q=80&w=400',
      },
      memo: '친구들과 맥주',
      file: 'https://api.app.com/receipts/194.png',
    },
    {
      expenseId: 193,
      date: '2024-02-07T12:00:00',
      merchantName: 'London Underground',
      categoryCode: '교통비',
      localCurrency: 'GBP',
      localAmount: 2.8,
      standardCurrency: 'KRW',
      standardAmount: 4760,
      exchangeRate: 1700,
      paymentMethod: {
        isCash: false,
        card: {
          company: { name: 'Visa', iconUrl: '' },
          label: '애플페이',
          lastDigits: '0000',
        },
      },
      travel: {
        id: 4,
        name: '영국 어학연수',
        imageUrl:
          'https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?q=80&w=400',
      },
      memo: '튜브 이동',
      file: '',
    },

    // --- 2024년 2월 06일 ---
    {
      expenseId: 192,
      date: '2024-02-06T19:00:00',
      merchantName: 'Woolworths Sydney',
      categoryCode: '생활',
      localCurrency: 'AUD',
      localAmount: 55.2,
      standardCurrency: 'KRW',
      standardAmount: 49680,
      exchangeRate: 900,
      paymentMethod: {
        isCash: false,
        card: {
          company: { name: 'Mastercard', iconUrl: '' },
          label: '트래블월렛',
          lastDigits: '3344',
        },
      },
      travel: {
        id: 5,
        name: '시드니 한 달 살기',
        imageUrl:
          'https://images.unsplash.com/photo-1506973035872-a4ec16b8e8d9?q=80&w=400',
      },
      memo: '식재료 장보기',
      file: 'https://api.app.com/receipts/192.png',
    },
    {
      expenseId: 191,
      date: '2024-02-06T08:30:00',
      merchantName: 'Sydney Cafe',
      categoryCode: '식비',
      localCurrency: 'AUD',
      localAmount: 6.5,
      standardCurrency: 'KRW',
      standardAmount: 5850,
      exchangeRate: 900,
      paymentMethod: { isCash: true, card: null },
      travel: {
        id: 5,
        name: '시드니 한 달 살기',
        imageUrl:
          'https://images.unsplash.com/photo-1506973035872-a4ec16b8e8d9?q=80&w=400',
      },
      memo: '플랫 화이트',
      file: '',
    },

    // --- 2024년 2월 05일 ---
    {
      expenseId: 190,
      date: '2024-02-05T15:00:00',
      merchantName: 'UCLA Store',
      categoryCode: '학교',
      localCurrency: 'USD',
      localAmount: 75.0,
      standardCurrency: 'KRW',
      standardAmount: 100500,
      exchangeRate: 1340,
      paymentMethod: {
        isCash: false,
        card: {
          company: { name: 'Visa', iconUrl: '' },
          label: '부모님 카드',
          lastDigits: '8811',
        },
      },
      travel: {
        id: 1,
        name: '뉴욕 교환학생',
        imageUrl:
          'https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?q=80&w=400',
      },
      memo: '전공 서적 구입',
      file: 'https://api.app.com/receipts/190.png',
    },
    {
      expenseId: 189,
      date: '2024-02-05T11:00:00',
      merchantName: 'AT&T Mobility',
      categoryCode: '통신비',
      localCurrency: 'USD',
      localAmount: 50.0,
      standardCurrency: 'KRW',
      standardAmount: 67000,
      exchangeRate: 1340,
      paymentMethod: {
        isCash: false,
        card: {
          company: { name: 'Visa', iconUrl: '' },
          label: '신한 체인지업',
          lastDigits: '4455',
        },
      },
      travel: {
        id: 1,
        name: '뉴욕 교환학생',
        imageUrl:
          'https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?q=80&w=400',
      },
      memo: '2월 통신료',
      file: '',
    },

    // --- 2024년 2월 04일 ---
    {
      expenseId: 188,
      date: '2024-02-04T18:00:00',
      merchantName: 'Osaka Aquarium',
      categoryCode: '여가',
      localCurrency: 'JPY',
      localAmount: 2700,
      standardCurrency: 'KRW',
      standardAmount: 24300,
      exchangeRate: 9.0,
      paymentMethod: {
        isCash: false,
        card: {
          company: { name: 'Mastercard', iconUrl: '' },
          label: '트래블월렛',
          lastDigits: '1122',
        },
      },
      travel: {
        id: 2,
        name: '오사카 미식 여행',
        imageUrl:
          'https://images.unsplash.com/photo-1590559899731-a382839e5549?q=80&w=400',
      },
      memo: '수족관 입장권',
      file: 'https://api.app.com/receipts/188.png',
    },
    {
      expenseId: 187,
      date: '2024-02-04T12:00:00',
      merchantName: 'Ichiran Ramen',
      categoryCode: '식비',
      localCurrency: 'JPY',
      localAmount: 1100,
      standardCurrency: 'KRW',
      standardAmount: 9900,
      exchangeRate: 9.0,
      paymentMethod: { isCash: true, card: null },
      travel: {
        id: 2,
        name: '오사카 미식 여행',
        imageUrl:
          'https://images.unsplash.com/photo-1590559899731-a382839e5549?q=80&w=400',
      },
      memo: '점심 식사',
      file: 'https://api.app.com/receipts/187.png',
    },

    // --- 2024년 2월 03일 ---
    {
      expenseId: 186,
      date: '2024-02-03T20:00:00',
      merchantName: 'Starbucks Berlin',
      categoryCode: '식비',
      localCurrency: 'EUR',
      localAmount: 5.8,
      standardCurrency: 'KRW',
      standardAmount: 8410,
      exchangeRate: 1450,
      paymentMethod: {
        isCash: false,
        card: {
          company: { name: 'Visa', iconUrl: '' },
          label: '트래블로그',
          lastDigits: '5566',
        },
      },
      travel: {
        id: 6,
        name: '베를린 건축 기행',
        imageUrl:
          'https://images.unsplash.com/photo-1560969184-10fe8719e047?q=80&w=400',
      },
      memo: '커피 한 잔',
      file: '',
    },
    {
      expenseId: 185,
      date: '2024-02-03T14:00:00',
      merchantName: 'Deutsche Bahn',
      categoryCode: '교통비',
      localCurrency: 'EUR',
      localAmount: 45.0,
      standardCurrency: 'KRW',
      standardAmount: 65250,
      exchangeRate: 1450,
      paymentMethod: {
        isCash: false,
        card: {
          company: { name: 'Mastercard', iconUrl: '' },
          label: '현대 제로',
          lastDigits: '4567',
        },
      },
      travel: {
        id: 6,
        name: '베를린 건축 기행',
        imageUrl:
          'https://images.unsplash.com/photo-1560969184-10fe8719e047?q=80&w=400',
      },
      memo: '기차 티켓',
      file: 'https://api.app.com/receipts/185.png',
    },

    // --- 2024년 2월 02일 ---
    {
      expenseId: 184,
      date: '2024-02-02T19:00:00',
      merchantName: 'Tesco London',
      categoryCode: '생활',
      localCurrency: 'GBP',
      localAmount: 32.5,
      standardCurrency: 'KRW',
      standardAmount: 55250,
      exchangeRate: 1700,
      paymentMethod: { isCash: true, card: null },
      travel: {
        id: 4,
        name: '영국 어학연수',
        imageUrl:
          'https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?q=80&w=400',
      },
      memo: '생필품 장보기',
      file: 'https://api.app.com/receipts/184.png',
    },
    {
      expenseId: 183,
      date: '2024-02-02T11:00:00',
      merchantName: 'Pret A Manger',
      categoryCode: '식비',
      localCurrency: 'GBP',
      localAmount: 8.2,
      standardCurrency: 'KRW',
      standardAmount: 13940,
      exchangeRate: 1700,
      paymentMethod: {
        isCash: false,
        card: {
          company: { name: 'Visa', iconUrl: '' },
          label: '트래블로그',
          lastDigits: '5566',
        },
      },
      travel: {
        id: 4,
        name: '영국 어학연수',
        imageUrl:
          'https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?q=80&w=400',
      },
      memo: '점심 샌드위치',
      file: '',
    },

    // --- 2024년 2월 01일 ---
    {
      expenseId: 182,
      date: '2024-02-01T21:00:00',
      merchantName: 'Hotel NH Berlin',
      categoryCode: '거주',
      localCurrency: 'EUR',
      localAmount: 95.0,
      standardCurrency: 'KRW',
      standardAmount: 137750,
      exchangeRate: 1450,
      paymentMethod: {
        isCash: false,
        card: {
          company: { name: 'Mastercard', iconUrl: '' },
          label: '현대 제로',
          lastDigits: '4567',
        },
      },
      travel: {
        id: 6,
        name: '베를린 건축 기행',
        imageUrl:
          'https://images.unsplash.com/photo-1560969184-10fe8719e047?q=80&w=400',
      },
      memo: '호텔 체크인',
      file: 'https://api.app.com/receipts/182.png',
    },
    {
      expenseId: 181,
      date: '2024-02-01T15:00:00',
      merchantName: 'Berlin Museum Island',
      categoryCode: '여가',
      localCurrency: 'EUR',
      localAmount: 18.0,
      standardCurrency: 'KRW',
      standardAmount: 26100,
      exchangeRate: 1450,
      paymentMethod: { isCash: true, card: null },
      travel: {
        id: 6,
        name: '베를린 건축 기행',
        imageUrl:
          'https://images.unsplash.com/photo-1560969184-10fe8719e047?q=80&w=400',
      },
      memo: '박물관 입장권',
      file: '',
    },
  ];
}
