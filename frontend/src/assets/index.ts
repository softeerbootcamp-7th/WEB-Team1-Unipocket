import Google from '@/assets/authLogos/google.svg';
import Guest from '@/assets/authLogos/guest.svg';
import Kakao from '@/assets/authLogos/kakao.svg';

export const AuthLogos = {
  Google,
  Kakao,
  Guest,
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
import Alert from '@/assets/Icons/alert.svg';
import AlertCircle from '@/assets/Icons/alert-circle.svg';
import Analytics from '@/assets/Icons/analytics.svg';
import Arrow from '@/assets/Icons/arrow.svg';
import Calendar from '@/assets/Icons/calendar.svg';
import Camera from '@/assets/Icons/camera.svg';
import CaretDown from '@/assets/Icons/caret-down.svg';
import Checkmark from '@/assets/Icons/checkmark.svg';
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
import Search from '@/assets/Icons/search.svg';
import Swap from '@/assets/Icons/swap.svg';
import SwapVertical from '@/assets/Icons/swap-vertical.svg';
import Trash from '@/assets/Icons/trash.svg';
import Travel from '@/assets/Icons/travel.svg';
import Update from '@/assets/Icons/update.svg';
import UploadFile from '@/assets/Icons/upload-file.svg';
import Warning from '@/assets/Icons/warning.svg';

export const Icons = {
  Add,
  Alert,
  Arrow,
  AlertCircle,
  CloseCircle,
  Calendar,
  ChevronBack,
  ChevronForward,
  Checkmark,
  CheckmarkCircle,
  CloseButton,
  Expand,
  Logo,
  Loading,
  Analytics,
  Home,
  Travel,
  Trash,
  SwapVertical,
  CaretDown,
  Close,
  Update,
  Refresh,
  Phone,
  Camera,
  FileBox,
  Edit,
  Search,
  Swap,
  LogoText,
  UploadFile,
  Information,
  Warning,
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
