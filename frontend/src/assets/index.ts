// 1. Bank Logos (한글 파일명 -> 영어 변수명 매핑)
import NHBank from '@/assets/bankLogos/농협.svg';
import ShinhanBank from '@/assets/bankLogos/신한.svg';
import KakaoBank from '@/assets/bankLogos/카카오뱅크.svg';
import TossBank from '@/assets/bankLogos/토스.svg';
import TravelWallet from '@/assets/bankLogos/트레블월렛.svg';
import HanaBank from '@/assets/bankLogos/하나.svg';
import HyundaiCard from '@/assets/bankLogos/현대카드.svg';

export const BankLogos = {
  NH: NHBank,
  Shinhan: ShinhanBank,
  Kakao: KakaoBank,
  Toss: TossBank,
  TravelWallet: TravelWallet,
  Hana: HanaBank,
  Hyundai: HyundaiCard,
} as const;

import Mingyu from '@/assets/authLogos/mingyu.svg';
import Google from '@/assets/authLogos/google.svg';
import Kakao from '@/assets/authLogos/kakao.svg';

export const AuthLogos = {
  Google,
  Kakao,
  Mingyu,
} as const;

// 2. Cards
import CardDefault from '@/assets/cards/Default.svg';
import CardDefault2 from '@/assets/cards/Variant2.svg';
import CardDefault3 from '@/assets/cards/Variant3.svg';

export const Cards = {
  Default: CardDefault,
  Variant2: CardDefault2,
  Variant3: CardDefault3,
} as const;

// 3. Icons
import AlertCircle from '@/assets/Icons/alert-circle.svg';
import Analytics from '@/assets/Icons/analytics.svg';
import Calendar from '@/assets/Icons/calendar.svg';
import Camera from '@/assets/Icons/camera.svg';
import CaretDown from '@/assets/Icons/caret-down.svg';
import CheckmarkCircle from '@/assets/Icons/checkmark-circle.svg';
import ChevronBack from '@/assets/Icons/chevron-back.svg';
import ChevronForward from '@/assets/Icons/chevron-forward.svg';
import Close from '@/assets/Icons/close.svg';
import CloseCircle from '@/assets/Icons/close-circle.svg';
import Edit from '@/assets/Icons/edit.svg';
import FileBox from '@/assets/Icons/file-box.svg';
import Home from '@/assets/Icons/home.svg';
import Information from '@/assets/Icons/information.svg';
import Loading from '@/assets/Icons/loading.svg';
import Logo from '@/assets/Icons/logo.svg';
import LogoText from '@/assets/Icons/logo-text.svg';
import Phone from '@/assets/Icons/phone.svg';
import Refresh from '@/assets/Icons/refresh.svg';
import Swap from '@/assets/Icons/swap.svg';
import Travel from '@/assets/Icons/travel.svg';
import UploadFile from '@/assets/Icons/upload-file.svg';
import SwapVertical from '@/assets/Icons/swap-vertical.svg';

export const Icons = {
  AlertCircle,
  CloseCircle,
  Calendar,
  ChevronBack,
  ChevronForward,
  CheckmarkCircle,
  Logo,
  Loading,
  Analytics,
  Home,
  Travel,
  SwapVertical,
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
