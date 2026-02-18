import { Suspense, useEffect, useState } from 'react';
import { useMatches } from '@tanstack/react-router';

import Button from '@/components/common/Button';
import ProfilePopover from '@/components/common/ProfilePopover';
import AccountBookSelector from '@/components/layout/AccountBookSelector';
import { Skeleton } from '@/components/ui/skeleton';

import { Icons } from '@/assets';
import { getLocalTime } from '@/lib/utils';

const Header = () => {
  const matches = useMatches();
  const isInitPath = matches.some((match) => match.routeId === '/_app/init');

  const [time, setTime] = useState(getLocalTime('KR'));

  // 1분마다 한국 시간 갱신
  useEffect(() => {
    const interval = setInterval(() => {
      setTime(getLocalTime('KR'));
    }, 60_000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="border-line-solid-normal sticky top-0 z-(--z-header) flex justify-between border-b px-8 py-3">
      <div className="flex items-center">
        {!isInitPath && (
          <Suspense fallback={<Skeleton className="h-10 w-40" />}>
            <AccountBookSelector />
          </Suspense>
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
