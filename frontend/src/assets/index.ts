// 1. Bank Logos (한글 파일명 -> 영어 변수명 매핑)
import NHBank from './bankLogos/농협.svg';
import ShinhanBank from './bankLogos/신한.svg';
import KakaoBank from './bankLogos/카카오뱅크.svg';
import TossBank from './bankLogos/토스.svg';
import TravelWallet from './bankLogos/트레블월렛.svg';
import HanaBank from './bankLogos/하나.svg';
import HyundaiCard from './bankLogos/현대카드.svg';

export const BankLogos = {
  NH: NHBank,
  Shinhan: ShinhanBank,
  Kakao: KakaoBank,
  Toss: TossBank,
  TravelWallet: TravelWallet,
  Hana: HanaBank,
  Hyundai: HyundaiCard,
};

// 2. Cards
import CardDefault from './cards/Default.svg';
import CardDefault2 from './cards/Variant2.svg';
import CardDefault3 from './cards/Variant3.svg';

export const Cards = {
  Default: CardDefault,
  Variant2: CardDefault2,
  Variant3: CardDefault3,
} as const;

// 3. Icons
import AlertCircle from './Icons/alert-circle.svg';
import Analytics from './Icons/analytics.svg';
import Camera from './Icons/camera.svg';
import CaretDown from './Icons/caret-down.svg';
import Close from './Icons/close.svg';
import CloseCircle from './Icons/close-circle.svg';
import Edit from './Icons/edit.svg';
import FileBox from './Icons/file-box.svg';
import Home from './Icons/home.svg';
import Logo from './Icons/Logo.svg';
import Phone from './Icons/phone.svg';
import Refresh from './Icons/refresh.svg';
import Travel from './Icons/travel.svg';

export const Icons = {
  AlertCircle,
  CloseCircle,
  Logo,
  Analytics,
  Home,
  Travel,
  CaretDown,
  Close,
  Refresh,
  Phone,
  Camera,
  FileBox,
  Edit,
} as const;

export type IconName = keyof typeof Icons;
