import {
  Popover,
  PopoverClose,
  PopoverContent,
  PopoverTrigger,
} from '@radix-ui/react-popover';
import { Link, useMatchRoute, useNavigate } from '@tanstack/react-router';
import { clsx } from 'clsx';

import Divider from '@/components/common/Divider';

import { useLogoutMutation } from '@/api/auth/query';
import { useGetUserQuery } from '@/api/users/query';
import ProfileImage from '@/assets/images/profile.png';
import { AUTH_PROVIDERS } from '@/constants/authProviders';
import { useAccountBookStore } from '@/stores/useAccountBookStore';

const ProfilePopover = () => {
  const { data } = useGetUserQuery();
  const { clearAccountBook } = useAccountBookStore.getState();
  const logoutMutation = useLogoutMutation();
  const navigator = useNavigate();
  const matchRoute = useMatchRoute();
  const isInitPath = !!matchRoute({ to: '/init' });

  const isGoogleUser = data.profileImgUrl?.includes('googleusercontent');
  const authProvider = AUTH_PROVIDERS.find(
    (p) => p.id === (isGoogleUser ? 'google' : 'kakao'),
  )!;
  const AuthIcon = authProvider.Icon;

  const handleLogout = (e: React.MouseEvent) => {
    e.preventDefault();
    logoutMutation.mutate(undefined, {
      onSuccess: () => {
        clearAccountBook();
        navigator({ to: '/' });
      },
    });
    return;
  };

  return (
    <Popover>
      <PopoverTrigger asChild>
        <img
          src={data.profileImgUrl || ProfileImage}
          alt="프로필 이미지"
          className="h-8 w-8 cursor-pointer rounded-full object-cover"
          referrerPolicy="no-referrer"
        />
      </PopoverTrigger>
      <PopoverContent
        align="end"
        className="mt-2"
        onOpenAutoFocus={(e) => e.preventDefault()}
      >
        <div className="bg-background-normal shadow-popover rounded-modal-18 flex min-w-60 flex-col gap-5 p-8">
          <div className="flex flex-col gap-2">
            <div className="flex items-center gap-2.5">
              {!data.email && (
                <AuthIcon
                  className={clsx(
                    'flex size-4 items-center justify-center rounded p-0.75',
                    authProvider.bgColor,
                  )}
                />
              )}
              <span className="body2-normal-bold">{data.name}</span>
            </div>
            {data.email && (
              <div className="flex items-center gap-2.5">
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
            )}
          </div>
          <Divider style={'thin'} />
          <PopoverClose asChild>
            {!isInitPath && (
              <Link
                to={'/setting'}
                className="body2-normal-medium text-label-alternative"
              >
                설정
              </Link>
            )}
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
