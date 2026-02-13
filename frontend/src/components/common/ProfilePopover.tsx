import {
  Popover,
  PopoverClose,
  PopoverContent,
  PopoverTrigger,
} from '@radix-ui/react-popover';
import { Link, useNavigate } from '@tanstack/react-router';
import { clsx } from 'clsx';

import Divider from '@/components/common/Divider';

import { useGetUserQuery, useLogoutMutation } from '@/api/auth/query';
import ProfileImage from '@/assets/images/profile.png';
import { AUTH_PROVIDERS } from '@/constants/authProviders';

const ProfilePopover = () => {
  const { data } = useGetUserQuery();
  const logoutMutation = useLogoutMutation();
  const navigator = useNavigate();

  const isGoogleUser = data.profileImgUrl?.includes('googleusercontent');
  const authProvider = AUTH_PROVIDERS.find(
    (p) => p.id === (isGoogleUser ? 'google' : 'kakao'),
  )!;
  const AuthIcon = authProvider.Icon;

  const handleLogout = (e: React.MouseEvent) => {
    e.preventDefault();

    // 기존 로그아웃 API 호출 (임시 주석 처리)
    // logoutMutation.mutate();

    // 로그인할 때와 동일한 도메인 및 옵션 설정
    const domain = import.meta.env.PROD
      ? `domain=${import.meta.env.VITE_COOKIE_DOMAIN}; `
      : '';
    const baseOptions = `path=/; ${domain}Secure; SameSite=None; `;

    // max-age=0 (또는 과거의 expires)을 사용하여 쿠키 삭제
    document.cookie = `access_token=; max-age=0; ${baseOptions}`;
    document.cookie = `refresh_token=; max-age=0; ${baseOptions}`;

    // 로그아웃 후 로그인 페이지나 메인으로 이동 (필요시 주석 해제하여 사용하세요)
    navigator({ to: '/login' });
  };

  return (
    <Popover>
      <PopoverTrigger asChild>
        <img
          src={data.profileImgUrl || ProfileImage}
          alt="프로필 이미지"
          className="h-8 w-8 cursor-pointer rounded-full object-cover"
          referrerPolicy="no-referrer"
          crossOrigin="anonymous"
        />
      </PopoverTrigger>
      <PopoverContent
        align="end"
        className="mt-2"
        onOpenAutoFocus={(e) => e.preventDefault()}
      >
        <div className="bg-background-normal shadow-popover rounded-modal-18 flex flex-col gap-5 p-8">
          <div className="flex flex-col gap-2">
            <span className="body2-normal-bold">{data.name}</span>
            <div className="flex gap-2.5">
              <AuthIcon
                className={clsx(
                  'flex size-4 items-center justify-center rounded p-0.75',
                  authProvider.bgColor,
                )}
              />
              <span className="label1-normal-medium text-label-alternative">
                {data.email}
              </span>
            </div>
          </div>
          <Divider style={'thin'} />
          <PopoverClose asChild>
            <Link
              to={'/setting'}
              className="body2-normal-medium text-label-alternative"
            >
              설정
            </Link>
          </PopoverClose>
          <PopoverClose asChild>
            <button
              onClick={handleLogout}
              disabled={logoutMutation.isPending}
              className="body2-normal-medium text-label-alternative cursor-pointer text-left"
            >
              로그아웃
            </button>
          </PopoverClose>
        </div>
      </PopoverContent>
    </Popover>
  );
};

export default ProfilePopover;
