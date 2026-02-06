import { clsx } from 'clsx';

import { API_BASE_URL } from '@/api/config/constants';
import { AuthLogos } from '@/assets';

const AUTH_PROVIDERS = [
  {
    id: 'kakao',
    text: '카카오 로그인',
    bgColor: 'bg-kakao-bg',
    textColor: 'text-kakao-text',
    Icon: AuthLogos.Kakao,
    href: `${API_BASE_URL}/auth/oauth2/authorize/kakao`,
  },
  {
    id: 'google',
    text: '구글 로그인',
    bgColor: 'bg-fill-normal',
    textColor: 'text-label-normal/54',
    Icon: AuthLogos.Google,
    href: `${API_BASE_URL}/auth/oauth2/authorize/google`,
  },
] as const;

interface LoginButtonProps {
  text: string;
  bgColor: string;
  textColor: string;
  Icon: React.ComponentType<{ className: string }>;
  href: string;
}

const LoginButton = ({
  text,
  bgColor,
  textColor,
  Icon,
  href,
}: LoginButtonProps) => (
  <a
    href={href}
    className={clsx(
      'flex items-center justify-center gap-3.5 rounded-lg py-[11.5px] transition-opacity hover:opacity-80',
      bgColor,
      textColor,
    )}
  >
    <Icon className="h-4.5 w-4.5" />
    <span className="text-[15px] font-semibold">{text}</span>
  </a>
);

const LoginContainer = () => {
  return (
    <>
      {AUTH_PROVIDERS.map((provider) => (
        <LoginButton key={provider.id} {...provider} />
      ))}
    </>
  );
};

export default LoginContainer;
