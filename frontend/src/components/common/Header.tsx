import { useEffect, useState } from 'react';
import { useMutation, useSuspenseQuery } from '@tanstack/react-query';
import { Link, useMatches, useNavigate } from '@tanstack/react-router';
import { clsx } from 'clsx';

import Dropdown from '@/components/common/dropdown/Dropdown';

import { logout } from '@/api/auth/api';
import { getUser } from '@/api/user/api';
import { Icons } from '@/assets';
import ProfileImage from '@/assets/images/profile.png';
import { AUTH_PROVIDERS } from '@/constants/authProviders';
import { getLocalTime } from '@/lib/utils';
import { queryClient } from '@/main';

import {
  Popover,
  PopoverClose,
  PopoverContent,
  PopoverTrigger,
} from '../ui/popover';
import Button from './Button';
import Divider from './Divider';

const ProfilePopover = () => {
  const navigate = useNavigate();
  const { data } = useSuspenseQuery({
    queryKey: ['getUser'],
    queryFn: getUser,
  });

  const logoutMutation = useMutation({
    mutationFn: logout,
    throwOnError: true, // 에러를 에러 바운더리로 전파
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['getUser'] });
      navigate({ to: '/login' });
    },
  });

  const isKakaoEmail = data.email?.includes('kakao');
  const authProvider = AUTH_PROVIDERS.find(
    (p) => p.id === (isKakaoEmail ? 'kakao' : 'google'),
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
                  'flex size-4 items-center justify-center rounded p-[3px]',
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

const Header = () => {
  const matches = useMatches();
  const isInitPath = matches.some((match) => match.routeId === '/_app/init');
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [time, setTime] = useState(getLocalTime('KR'));

  // @TODO: 추후 options API 연동
  const options = [
    { id: 1, name: '미국 교환학생' },
    { id: 2, name: '2025 캐나다' },
    { id: 3, name: '독일 교환학생' },
  ];

  // 1분마다 한국 시간 갱신
  useEffect(() => {
    const interval = setInterval(() => {
      setTime(getLocalTime('KR'));
    }, 60_000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="border-line-solid-normal sticky top-0 z-10 flex justify-between border-b px-8 py-3">
      <div className="flex items-center">
        {!isInitPath && (
          <Dropdown
            selected={selectedId}
            onSelect={setSelectedId}
            options={options}
          />
        )}
      </div>
      <div className="flex items-center gap-5">
        {!isInitPath && (
          <div className="flex items-center gap-2">
            <span className="label2-medium text-label-neutral">{time}</span>
            <Icons.Refresh className="text-label-neutral h-4 w-4 cursor-pointer" />
          </div>
        )}
        <Button onClick={() => {}}>모바일</Button>
        <ProfilePopover />
      </div>
    </div>
  );
};

export default Header;
