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
    // --- 2월 6일 데이터 ---
    {
      id: 120,
      date: '2024-02-06T20:00:00',
      amount: 4500,
      currency: 'JPY',
      krwAmount: 40500,
      storeName: '이자카야 토리키조쿠',
      memo: '현지인 맛집 탐방',
      hasReceipt: true,
      travel: { id: 10, name: '오사카 식도락 여행' },
      budget: { id: 55, name: '오사카 메인 예산', countryCode: 'JP' },
      category: {
        code: 'FOOD',
        name: '식비',
        iconUrl: 'https://api.app.com/icons/food.png',
      },
    },
    {
      id: 119,
      date: '2024-02-06T15:00:00',
      amount: 8500,
      currency: 'JPY',
      krwAmount: 76500,
      storeName: '돈키호테 도톤보리점',
      memo: '선물용 젤리 및 의약품',
      hasReceipt: true,
      travel: { id: 10, name: '오사카 식도락 여행' },
      budget: { id: 55, name: '오사카 메인 예산', countryCode: 'JP' },
      category: {
        code: 'SHOPPING',
        name: '쇼핑',
        iconUrl: 'https://api.app.com/icons/shop.png',
      },
    },
    {
      id: 118,
      date: '2024-02-06T10:00:00',
      amount: 280,
      currency: 'JPY',
      krwAmount: 2520,
      storeName: '오사카 지하철',
      memo: '난바역 이동',
      hasReceipt: false,
      travel: { id: 10, name: '오사카 식도락 여행' },
      budget: { id: 55, name: '오사카 메인 예산', countryCode: 'JP' },
      category: {
        code: 'TRANSPORT',
        name: '교통',
        iconUrl: 'https://api.app.com/icons/bus.png',
      },
    },

    // --- 2월 5일 데이터 ---
    {
      id: 117,
      date: '2024-02-05T21:30:00',
      amount: 15000,
      currency: 'JPY',
      krwAmount: 135000,
      storeName: '호텔 그레이스리 오사카',
      memo: '2박 숙박비 결제',
      hasReceipt: true,
      travel: { id: 10, name: '오사카 식도락 여행' },
      budget: { id: 55, name: '오사카 메인 예산', countryCode: 'JP' },
      category: {
        code: 'LODGING',
        name: '숙박',
        iconUrl: 'https://api.app.com/icons/hotel.png',
      },
    },
    {
      id: 116,
      date: '2024-02-05T18:00:00',
      amount: 1200,
      currency: 'JPY',
      krwAmount: 10800,
      storeName: '이치란 라멘',
      memo: '차슈 추가',
      hasReceipt: true,
      travel: { id: 10, name: '오사카 식도락 여행' },
      budget: { id: 55, name: '오사카 메인 예산', countryCode: 'JP' },
      category: {
        code: 'FOOD',
        name: '식비',
        iconUrl: 'https://api.app.com/icons/food.png',
      },
    },
    {
      id: 115,
      date: '2024-02-05T14:00:00',
      amount: 7000,
      currency: 'JPY',
      krwAmount: 63000,
      storeName: '유니버셜 스튜디오 재팬',
      memo: '입장권',
      hasReceipt: true,
      travel: { id: 10, name: '오사카 식도락 여행' },
      budget: { id: 55, name: '오사카 메인 예산', countryCode: 'JP' },
      category: {
        code: 'CULTURE',
        name: '관광',
        iconUrl: 'https://api.app.com/icons/tour.png',
      },
    },
    {
      id: 114,
      date: '2024-02-05T11:00:00',
      amount: 600,
      currency: 'JPY',
      krwAmount: 5400,
      storeName: '스타벅스',
      memo: '아메리카노 한 잔',
      hasReceipt: true,
      travel: { id: 10, name: '오사카 식도락 여행' },
      budget: { id: 55, name: '오사카 메인 예산', countryCode: 'JP' },
      category: {
        code: 'CAFE',
        name: '카페',
        iconUrl: 'https://api.app.com/icons/cafe.png',
      },
    },

    // --- 2월 4일 데이터 ---
    {
      id: 113,
      date: '2024-02-04T19:00:00',
      amount: 3500,
      currency: 'JPY',
      krwAmount: 31500,
      storeName: '쿠시카츠 다루마',
      memo: '튀김 세트',
      hasReceipt: true,
      travel: { id: 10, name: '오사카 식도락 여행' },
      budget: { id: 55, name: '오사카 메인 예산', countryCode: 'JP' },
      category: {
        code: 'FOOD',
        name: '식비',
        iconUrl: 'https://api.app.com/icons/food.png',
      },
    },
    {
      id: 112,
      date: '2024-02-04T12:30:00',
      amount: 500,
      currency: 'JPY',
      krwAmount: 4500,
      storeName: '로손 편의점',
      memo: '모찌롤',
      hasReceipt: false,
      travel: { id: 10, name: '오사카 식도락 여행' },
      budget: { id: 55, name: '오사카 메인 예산', countryCode: 'JP' },
      category: {
        code: 'SNACK',
        name: '간식',
        iconUrl: 'https://api.app.com/icons/snack.png',
      },
    },
    {
      id: 111,
      date: '2024-02-04T09:00:00',
      amount: 1100,
      currency: 'JPY',
      krwAmount: 9900,
      storeName: '라피트 특급열차',
      memo: '공항에서 시내로 이동',
      hasReceipt: true,
      travel: { id: 10, name: '오사카 식도락 여행' },
      budget: { id: 55, name: '오사카 메인 예산', countryCode: 'JP' },
      category: {
        code: 'TRANSPORT',
        name: '교통',
        iconUrl: 'https://api.app.com/icons/bus.png',
      },
    },

    // --- 추가 데이터 (ID 101~110) ---
    {
      id: 110,
      date: '2024-02-03T18:00:00',
      amount: 2500,
      currency: 'JPY',
      krwAmount: 22500,
      storeName: '빅카메라',
      memo: '보조배터리 구매',
      hasReceipt: true,
      travel: { id: 10, name: '오사카 식도락 여행' },
      budget: { id: 55, name: '오사카 메인 예산', countryCode: 'JP' },
      category: {
        code: 'SHOPPING',
        name: '쇼핑',
        iconUrl: 'https://api.app.com/icons/shop.png',
      },
    },
    {
      id: 109,
      date: '2024-02-03T13:00:00',
      amount: 1500,
      currency: 'JPY',
      krwAmount: 13500,
      storeName: '우오신 스시',
      memo: '대왕 스시 점심',
      hasReceipt: true,
      travel: { id: 10, name: '오사카 식도락 여행' },
      budget: { id: 55, name: '오사카 메인 예산', countryCode: 'JP' },
      category: {
        code: 'FOOD',
        name: '식비',
        iconUrl: 'https://api.app.com/icons/food.png',
      },
    },
    {
      id: 108,
      date: '2024-02-03T10:00:00',
      amount: 800,
      currency: 'JPY',
      krwAmount: 7200,
      storeName: '교토 버스 일일권',
      memo: '당일치기 교토 이동',
      hasReceipt: false,
      travel: { id: 10, name: '오사카 식도락 여행' },
      budget: { id: 55, name: '오사카 메인 예산', countryCode: 'JP' },
      category: {
        code: 'TRANSPORT',
        name: '교통',
        iconUrl: 'https://api.app.com/icons/bus.png',
      },
    },
    {
      id: 107,
      date: '2024-02-02T20:00:00',
      amount: 3000,
      currency: 'JPY',
      krwAmount: 27000,
      storeName: '우메다 스카이빌딩',
      memo: '공중정원 입장료',
      hasReceipt: true,
      travel: { id: 10, name: '오사카 식도락 여행' },
      budget: { id: 55, name: '오사카 메인 예산', countryCode: 'JP' },
      category: {
        code: 'CULTURE',
        name: '관광',
        iconUrl: 'https://api.app.com/icons/tour.png',
      },
    },
    {
      id: 106,
      date: '2024-02-02T16:00:00',
      amount: 450,
      currency: 'JPY',
      krwAmount: 4050,
      storeName: '자판기',
      memo: '이로하스 복숭아물',
      hasReceipt: false,
      travel: { id: 10, name: '오사카 식도락 여행' },
      budget: { id: 55, name: '오사카 메인 예산', countryCode: 'JP' },
      category: {
        code: 'SNACK',
        name: '간식',
        iconUrl: 'https://api.app.com/icons/snack.png',
      },
    },
    {
      id: 105,
      date: '2024-02-02T12:00:00',
      amount: 980,
      currency: 'JPY',
      krwAmount: 8820,
      storeName: '요시노야',
      memo: '규동 혼밥',
      hasReceipt: true,
      travel: { id: 10, name: '오사카 식도락 여행' },
      budget: { id: 55, name: '오사카 메인 예산', countryCode: 'JP' },
      category: {
        code: 'FOOD',
        name: '식비',
        iconUrl: 'https://api.app.com/icons/food.png',
      },
    },
    {
      id: 104,
      date: '2024-02-01T22:00:00',
      amount: 2100,
      currency: 'JPY',
      krwAmount: 18900,
      storeName: '드럭스토어',
      memo: '휴식시간 패치',
      hasReceipt: true,
      travel: { id: 10, name: '오사카 식도락 여행' },
      budget: { id: 55, name: '오사카 메인 예산', countryCode: 'JP' },
      category: {
        code: 'SHOPPING',
        name: '쇼핑',
        iconUrl: 'https://api.app.com/icons/shop.png',
      },
    },
    {
      id: 103,
      date: '2024-02-01T15:00:00',
      amount: 1500,
      currency: 'JPY',
      krwAmount: 13500,
      storeName: '오사카성',
      memo: '천수각 입장료',
      hasReceipt: true,
      travel: { id: 10, name: '오사카 식도락 여행' },
      budget: { id: 55, name: '오사카 메인 예산', countryCode: 'JP' },
      category: {
        code: 'CULTURE',
        name: '관광',
        iconUrl: 'https://api.app.com/icons/tour.png',
      },
    },
    {
      id: 102,
      date: '2024-02-01T10:00:00',
      amount: 300,
      currency: 'JPY',
      krwAmount: 2700,
      storeName: '패밀리마트',
      memo: '샌드위치',
      hasReceipt: false,
      travel: { id: 10, name: '오사카 식도락 여행' },
      budget: { id: 55, name: '오사카 메인 예산', countryCode: 'JP' },
      category: {
        code: 'SNACK',
        name: '간식',
        iconUrl: 'https://api.app.com/icons/snack.png',
      },
    },
    {
      id: 101,
      date: '2024-02-01T08:00:00',
      amount: 1000,
      currency: 'JPY',
      krwAmount: 9000,
      storeName: '간사이 공항',
      memo: '유심 카드 구매',
      hasReceipt: true,
      travel: { id: 10, name: '오사카 식도락 여행' },
      budget: { id: 55, name: '오사카 메인 예산', countryCode: 'JP' },
      category: {
        code: 'ETC',
        name: '기타',
        iconUrl: 'https://api.app.com/icons/etc.png',
      },
    },
  ];
}
