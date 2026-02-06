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
} as const;

import Google from './authLogos/google.svg';
import Kakao from './authLogos/kakao.svg';

export const AuthLogos = {
  Google,
  Kakao,
} as const;

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
import CheckmarkCircle from './Icons/checkmark-circle.svg';
import ChevronBack from './Icons/chevron-back.svg';
import ChevronForward from './Icons/chevron-forward.svg';
import Close from './Icons/close.svg';
import CloseCircle from './Icons/close-circle.svg';
import Edit from './Icons/edit.svg';
import FileBox from './Icons/file-box.svg';
import Home from './Icons/home.svg';
import Information from './Icons/information.svg';
import Loading from './Icons/loading.svg';
import Logo from './Icons/logo.svg';
import LogoText from './Icons/logo-text.svg';
import Phone from './Icons/phone.svg';
import Refresh from './Icons/refresh.svg';
import Swap from './Icons/swap.svg';
import Travel from './Icons/travel.svg';
import UploadFile from './Icons/upload-file.svg';

export const Icons = {
  AlertCircle,
  CloseCircle,
  ChevronBack,
  ChevronForward,
  CheckmarkCircle,
  Logo,
  Loading,
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
  Swap,
  LogoText,
  UploadFile,
  Information,
} as const;

export type IconName = keyof typeof Icons;

import DemoReceipt1 from '@/assets/images/landing/demo-receipt.png';
import FeaturePreview1 from '@/assets/images/landing/feature-1.png';
import FeaturePreview3 from '@/assets/images/landing/feature-3.png';
import FeaturePreview4 from '@/assets/images/landing/feature-4.png';
import HomePreview from '@/assets/images/landing/home-preview.png';

export const LandingImages = {
  DemoReceipt1,
  FeaturePreview1,
  FeaturePreview3,
  FeaturePreview4,
  HomePreview,
} as const;
