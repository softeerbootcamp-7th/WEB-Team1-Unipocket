import BCCard from '@/assets/bankLogos/BC.svg';
import HanaBank from '@/assets/bankLogos/HANA.svg';
import HyundaiCard from '@/assets/bankLogos/HYUNDAI.svg';
import IBKBank from '@/assets/bankLogos/IBK.svg';
import KakaoBank from '@/assets/bankLogos/KAKAO.svg';
import KBBank from '@/assets/bankLogos/KB.svg';
import LotteCard from '@/assets/bankLogos/LOTTE.svg';
import NHBank from '@/assets/bankLogos/NK.svg';
import SamsungCard from '@/assets/bankLogos/SAMSUNG.svg';
import ShinhanBank from '@/assets/bankLogos/SHINHAN.svg';
import TravelWallet from '@/assets/bankLogos/TAVEL_WALLET.svg';
import TossBank from '@/assets/bankLogos/TOSS.svg';
import WooriBank from '@/assets/bankLogos/WORRI.svg';

export const CARDS = {
  0: {
    code: 'SHINHAN',
    name: '신한카드',
    logo: ShinhanBank,
  },
  1: {
    code: 'SAMSUNG',
    name: '삼성카드',
    logo: SamsungCard,
  },
  2: {
    code: 'KB',
    name: 'KB국민카드',
    logo: KBBank,
  },
  3: {
    code: 'HYUNDAI',
    name: '현대카드',
    logo: HyundaiCard,
  },
  4: {
    code: 'LOTTE',
    name: '롯데카드',
    logo: LotteCard,
  },
  5: {
    code: 'WOORI',
    name: '우리카드',
    logo: WooriBank,
  },
  6: {
    code: 'HANA',
    name: '하나카드',
    logo: HanaBank,
  },
  7: {
    code: 'NH',
    name: 'NH농협카드',
    logo: NHBank,
  },
  8: {
    code: 'BC',
    name: 'BC카드',
    logo: BCCard,
  },
  9: {
    code: 'IBK',
    name: 'IBK기업은행',
    logo: IBKBank,
  },
  10: {
    code: 'KAKAO',
    name: '카카오뱅크',
    logo: KakaoBank,
  },
  11: {
    code: 'TOSS',
    name: '토스뱅크',
    logo: TossBank,
  },
  12: {
    code: 'TRAVEL_WALLET',
    name: '트레블월렛',
    logo: TravelWallet,
  },
} as const;

export type CardId = keyof typeof CARDS;
export type CardCode = (typeof CARDS)[CardId]['code'];
