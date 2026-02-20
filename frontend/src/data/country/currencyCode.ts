export const CURRENCY_CODE = {
  KRW: 'KRW', // 한국 원
  ZAR: 'ZAR', // 남아프리카 공화국 랜드
  NPR: 'NPR', // 네팔 루피
  NOK: 'NOK', // 노르웨이 크로네
  NZD: 'NZD', // 뉴질랜드 달러
  TWD: 'TWD', // 대만 달러
  DKK: 'DKK', // 덴마크 크로네
  RUB: 'RUB', // 러시아 루블
  MOP: 'MOP', // 마카오 파타카
  MYR: 'MYR', // 말레이시아 링깃
  MXN: 'MXN', // 멕시코 페소
  MNT: 'MNT', // 몽골 투그릭
  USD: 'USD', // 미국 달러
  BHD: 'BHD', // 바레인 디나르
  BDT: 'BDT', // 방글라데시 타카
  VND: 'VND', // 베트남 동
  BRL: 'BRL', // 브라질 헤알
  BND: 'BND', // 브루나이 달러
  SAR: 'SAR', // 사우디아라비아 리얄
  SEK: 'SEK', // 스웨덴 크로나
  CHF: 'CHF', // 스위스 프랑
  SGD: 'SGD', // 싱가포르 달러
  AED: 'AED', // 아랍에미리트 디르함
  GBP: 'GBP', // 영국 파운드
  OMR: 'OMR', // 오만 리알
  JOD: 'JOD', // 요르단 디나르
  EUR: 'EUR', // 유로 (독일, 프랑스, 이탈리아, 스페인 등 공통)
  ILS: 'ILS', // 이스라엘 셰켈
  EGP: 'EGP', // 이집트 파운드
  INR: 'INR', // 인도 루피
  IDR: 'IDR', // 인도네시아 루피아
  JPY: 'JPY', // 일본 엔
  CNY: 'CNY', // 중국 위안
  CZK: 'CZK', // 체코 코루나
  CLP: 'CLP', // 칠레 페소
  KZT: 'KZT', // 카자흐스탄 텐게
  QAR: 'QAR', // 카타르 리얄
  CAD: 'CAD', // 캐나다 달러
  KWD: 'KWD', // 쿠웨이트 디나르
  THB: 'THB', // 태국 바트
  TRY: 'TRY', // 터키 리라
  PKR: 'PKR', // 파키스탄 루피
  PLN: 'PLN', // 폴란드 즈워티
  PHP: 'PHP', // 필리핀 페소
  HUF: 'HUF', // 헝가리 포린트
  AUD: 'AUD', // 호주 달러
  HKD: 'HKD', // 홍콩 달러
} as const;

export type CurrencyCode = keyof typeof CURRENCY_CODE;
