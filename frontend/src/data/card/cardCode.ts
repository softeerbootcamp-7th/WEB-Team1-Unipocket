import HanaBank from '/public/assets/cards/HANA.svg';
import HyundaiCard from '/public/assets/cards/HYUNDAI.svg';
import IBKBank from '/public/assets/cards/IBK.svg';
import KakaoBank from '/public/assets/cards/KAKAO.svg';
import KBBank from '/public/assets/cards/KB.svg';
import LotteCard from '/public/assets/cards/LOTTE.svg';
import NHBank from '/public/assets/cards/NK.svg';
import SamsungCard from '/public/assets/cards/SAMSUNG.svg';
import ShinhanBank from '/public/assets/cards/SHINHAN.svg';
import TravelWallet from '/public/assets/cards/TAVEL_WALLET.svg';
import TossBank from '/public/assets/cards/TOSS.svg';
import WooriBank from '/public/assets/cards/WORRI.svg';
import BCCard from '/public/assets/flags/KR.svg';

export const CARDS = {
  0: {
    code: '신한',
    logo: ShinhanBank,
  },
  1: {
    code: '삼성',
    logo: SamsungCard,
  },
  2: {
    code: 'KB국민',
    logo: KBBank,
  },
  3: {
    code: '현대',
    logo: HyundaiCard,
  },
  4: {
    code: '롯데',
    logo: LotteCard,
  },
  5: {
    code: '우리',
    logo: WooriBank,
  },
  6: {
    code: '하나',
    logo: HanaBank,
  },
  7: {
    code: 'NH농협',
    logo: NHBank,
  },
  8: {
    code: 'BC',
    logo: BCCard,
  },
  9: {
    code: 'IBK',
    logo: IBKBank,
  },
  10: {
    code: '카카오',
    logo: KakaoBank,
  },
  11: {
    code: '토스',
    logo: TossBank,
  },
  12: {
    code: '트래블월렛',
    logo: TravelWallet,
  },
} as const;

export type CardId = keyof typeof CARDS;
export type CardCode = (typeof CARDS)[CardId]['code'];
