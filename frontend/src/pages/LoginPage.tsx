import LoginContainer from '@/components/login-page/LoginContainer';

import { Icons } from '@/assets';

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
        <div className="absolute right-20 bottom-23 left-20 flex flex-col gap-3 md:static md:w-81.25">
          <LoginContainer />
        </div>
      </div>
    </main>
  );
};

export default LoginPage;
