import { useEffect, useState } from 'react';
import { useMatches } from '@tanstack/react-router';

import Dropdown from '@/components/common/dropdown/Dropdown';

import { Icons } from '@/assets';
import { getLocalTime } from '@/lib/utils';

import Button from './Button';
import ProfilePopover from './ProfilePopover';

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
