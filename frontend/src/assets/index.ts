import Google from '@/assets/authLogos/google.svg';
import Kakao from '@/assets/authLogos/kakao.svg';
import Mingyu from '@/assets/authLogos/mingyu.svg';

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
import Add from '@/assets/Icons/add.svg';
import AlertCircle from '@/assets/Icons/alert-circle.svg';
import Analytics from '@/assets/Icons/analytics.svg';
import Arrow from '@/assets/Icons/arrow.svg';
import Calendar from '@/assets/Icons/calendar.svg';
import Camera from '@/assets/Icons/camera.svg';
import CaretDown from '@/assets/Icons/caret-down.svg';
import CheckmarkCircle from '@/assets/Icons/checkmark-circle.svg';
import ChevronBack from '@/assets/Icons/chevron-back.svg';
import ChevronForward from '@/assets/Icons/chevron-forward.svg';
import Close from '@/assets/Icons/close.svg';
import CloseButton from '@/assets/Icons/close-button.svg';
import CloseCircle from '@/assets/Icons/close-circle.svg';
import Edit from '@/assets/Icons/edit.svg';
import Expand from '@/assets/Icons/expand.svg';
import FileBox from '@/assets/Icons/file-box.svg';
import Home from '@/assets/Icons/home.svg';
import Information from '@/assets/Icons/information.svg';
import Loading from '@/assets/Icons/loading.svg';
import Logo from '@/assets/Icons/logo.svg';
import LogoText from '@/assets/Icons/logo-text.svg';
import Phone from '@/assets/Icons/phone.svg';
import Refresh from '@/assets/Icons/refresh.svg';
import Swap from '@/assets/Icons/swap.svg';
import SwapVertical from '@/assets/Icons/swap-vertical.svg';
import Travel from '@/assets/Icons/travel.svg';
import UploadFile from '@/assets/Icons/upload-file.svg';

export const Icons = {
  Add,
  Arrow,
  AlertCircle,
  CloseCircle,
  Calendar,
  ChevronBack,
  ChevronForward,
  CheckmarkCircle,
  CloseButton,
  Expand,
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
