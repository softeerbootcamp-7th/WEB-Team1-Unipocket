import { Link } from '@tanstack/react-router';
import clsx from 'clsx';

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

interface AuthButtonProps {
  text: string;
  bgColor: string;
  textColor: string;
  Icon: React.ComponentType<{ className: string }>;
  to: string;
}

const AuthButton = ({
  text,
  bgColor,
  textColor,
  Icon,
  to,
}: AuthButtonProps) => (
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

const LoginPage = () => {
  return (
    <main className="bg-cool-neutral-99 -mt-14.25 flex min-h-screen items-center justify-center">
      <div className="px-25 py-17.5 text-center">
        <h1 className="mb-4 text-[32px] font-medium">로그인</h1>
        <h2 className="mb-25 text-gray-400">
          소셜 로그인으로 간단하게 시작해보세요
        </h2>
        <div className="flex flex-col gap-3">
          {AUTH_PROVIDERS.map((provider) => (
            <AuthButton key={provider.id} {...provider} />
          ))}
        </div>
      </div>
    </main>
  );
};

export default LoginPage;
