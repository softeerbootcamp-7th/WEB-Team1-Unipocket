import { Suspense, useEffect, useState } from 'react';
import { useMatchRoute } from '@tanstack/react-router';

import ProfilePopover from '@/components/common/ProfilePopover';
import AccountBookSelector from '@/components/layout/AccountBookSelector';
import { Skeleton } from '@/components/ui/skeleton';

import { getLocalTime } from '@/lib/utils';

const Header = () => {
  const matchRoute = useMatchRoute();
  const isInitPath = !!matchRoute({ to: '/init' });

  const [time, setTime] = useState(getLocalTime('KR'));

  // 1분마다 한국 시간 갱신
  useEffect(() => {
    const interval = setInterval(() => {
      setTime(getLocalTime('KR'));
    }, 60_000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="border-line-solid-normal z-header sticky top-0 flex justify-between border-b px-8 py-3">
      <div className="flex items-center">
        {!isInitPath && (
          <Suspense fallback={<Skeleton className="h-10 w-40" />}>
            <AccountBookSelector />
          </Suspense>
        )}
      </div>
      <div className="flex items-center gap-5">
        {!isInitPath && (
          <span className="label2-medium text-label-neutral">{time}</span>
        )}
        {/* {!isInitPath && <Button onClick={() => {}}>모바일</Button>} */}
        <ProfilePopover />
      </div>
    </div>
  );
};

export default Header;
