import { Link } from '@tanstack/react-router';
import { clsx } from 'clsx';

import { AuthLogos } from '@/assets';

const AUTH_PROVIDERS = [
  {
    id: 'kakao',
    text: '카카오 로그인',
    bgColor: 'bg-kakao-bg',
    textColor: 'text-kakao-text',
    Icon: AuthLogos.Kakao,
    to: 'http://localhost:8080/api/auth/oauth2/authorize/kakao',
  },
  {
    id: 'google',
    text: '구글 로그인',
    bgColor: 'bg-fill-normal',
    textColor: 'text-label-normal/54',
    Icon: AuthLogos.Google,
    to: 'http://localhost:8080/api/auth/oauth2/authorize/google',
  },
] as const;

interface LoginButtonProps {
  text: string;
  bgColor: string;
  textColor: string;
  Icon: React.ComponentType<{ className: string }>;
  to: string;
}

const LoginButton = ({
  text,
  bgColor,
  textColor,
  Icon,
  to,
}: LoginButtonProps) => (
  <Link to={to} className="mx-auto block w-81.25">
    <div
      className={clsx(
        'flex items-center justify-center gap-3.5 rounded-lg py-[11.5px]',
        bgColor,
        textColor,
      )}
    >
      <Icon className="h-4.5 w-4.5" />
      <span className="text-[15px] font-semibold">{text}</span>
    </div>
  </Link>
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
