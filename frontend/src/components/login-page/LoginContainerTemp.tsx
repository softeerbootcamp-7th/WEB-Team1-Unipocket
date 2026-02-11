import { useNavigate } from '@tanstack/react-router';

import { loginDev } from '@/api/auth/api';
import { AuthLogos } from '@/assets';

const LoginContainerTemp = () => {
  const navigate = useNavigate();
  const handleOnClick = async () => {
    try {
      const { accessToken, refreshToken, expiresIn } = await loginDev();
      const domain = import.meta.env.PROD
        ? `domain=${import.meta.env.VITE_COOKIE_DOMAIN}; `
        : '';
      const baseOptions = `path=/; ${domain}Secure; SameSite=None; `;
      const REFRESH_EXPIRY = 30 * 24 * 60 * 60;
      document.cookie = `access_token=${accessToken}; max-age=${expiresIn}; ${baseOptions}`;
      document.cookie = `refresh_token=${refreshToken}; max-age=${REFRESH_EXPIRY}; ${baseOptions}`;
      navigate({ to: '/home' });
    } catch (error) {
      console.error('로그인 실패:', error);
    }
  };
  return (
    <button
      onClick={handleOnClick}
      className="bg-background-normal flex cursor-pointer items-center justify-center gap-3.5 rounded-lg py-[11.5px] transition-opacity hover:opacity-80"
    >
      <AuthLogos.Mingyu className="h-4.5 w-4.5" />
      <span className="text-primary-heavy text-[15px] font-semibold">
        성민규로 로그인
      </span>
    </button>
  );
};

export default LoginContainerTemp;
