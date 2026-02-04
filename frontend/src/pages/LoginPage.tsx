import LoginButton from '@/components/login-page/LoginButton';

import { AuthLogos, Icons } from '@/assets';

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

const LoginPage = () => {
  return (
    <main className="realative -mt-14.25 flex min-h-screen items-center justify-center md:static">
      <div className="px-25 py-17.5 text-center">
        <div className="hidden flex-col gap-1 md:mb-25 md:flex">
          <h1 className="text-[32px] font-medium">로그인</h1>
          <h2 className="text-label-alternative">
            소셜 로그인으로 간단하게 시작해보세요
          </h2>
        </div>
        <div className="flex flex-col items-center md:hidden">
          <Icons.Logo width={42} height={42} />
          <Icons.LogoText width={160} height={48} />
          <h1 className="label2-medium text-primary-normal mt-5">
            교환학생 지출의 모든 것, 통합 가계부 유니포켓
          </h1>
        </div>
        <div className="absolute right-0 bottom-23 left-0 flex flex-col gap-3 md:static">
          {AUTH_PROVIDERS.map((provider) => (
            <LoginButton key={provider.id} {...provider} />
          ))}
        </div>
      </div>
    </main>
  );
};

export default LoginPage;
