import {
  Popover,
  PopoverClose,
  PopoverContent,
  PopoverTrigger,
} from '@radix-ui/react-popover';
import { Link } from '@tanstack/react-router';
import { clsx } from 'clsx';

import Divider from '@/components/common/Divider';

import { useGetUserQuery, useLogoutMutation } from '@/api/auth/query';
import ProfileImage from '@/assets/images/profile.png';
import { AUTH_PROVIDERS } from '@/constants/authProviders';

const ProfilePopover = () => {
  const { data } = useGetUserQuery();
  const logoutMutation = useLogoutMutation();

  const isGoogleUser = data.profileImgUrl?.includes('googleusercontent');
  const authProvider = AUTH_PROVIDERS.find(
    (p) => p.id === (isGoogleUser ? 'google' : 'kakao'),
  )!;
  const AuthIcon = authProvider.Icon;

  const handleLogout = (e: React.MouseEvent) => {
    e.preventDefault();
    logoutMutation.mutate();
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
