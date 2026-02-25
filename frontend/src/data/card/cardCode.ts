const CDN_URL = import.meta.env.VITE_CDN_URL;

export const CARDS = {
  0: {
    code: '신한',
    logo: `${CDN_URL}/assets/cards/SHINHAN.svg`,
  },
  1: {
    code: '삼성',
    logo: `${CDN_URL}/assets/cards/SAMSUNG.svg`,
  },
  2: {
    code: 'KB국민',
    logo: `${CDN_URL}/assets/cards/KB.svg`,
  },
  3: {
    code: '현대',
    logo: `${CDN_URL}/assets/cards/HYUNDAI.svg`,
  },
  4: {
    code: '롯데',
    logo: `${CDN_URL}/assets/cards/LOTTE.svg`,
  },
  5: {
    code: '우리',
    logo: `${CDN_URL}/assets/cards/WORRI.svg`,
  },
  6: {
    code: '하나',
    logo: `${CDN_URL}/assets/cards/HANA.svg`,
  },
  7: {
    code: 'NH농협',
    logo: `${CDN_URL}/assets/cards/NK.svg`,
  },
  8: {
    code: 'BC',
    logo: `${CDN_URL}/assets/cards/BC.svg`,
  },
  9: {
    code: 'IBK',
    logo: `${CDN_URL}/assets/cards/IBK.svg`,
  },
  10: {
    code: '카카오',
    logo: `${CDN_URL}/assets/cards/KAKAO.svg`,
  },
  11: {
    code: '토스',
    logo: `${CDN_URL}/assets/cards/TOSS.svg`,
  },
} as const;

export type CardId = keyof typeof CARDS;
export type CardCode = (typeof CARDS)[CardId]['code'];

export const getCardInfo = (id: string | number) =>
  CARDS[Number(id) as CardId] ?? null;
