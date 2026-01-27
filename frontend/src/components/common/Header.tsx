import { useEffect, useState } from 'react';

import Dropdown from '@/components/common/dropdown/Dropdown';

import { Icons } from '@/assets';
import ProfileImage from '@/assets/images/profile.png';
import { getLocalTime } from '@/lib/utils';

import Button from './Button';

const Header = () => {
  const [selected, setSelected] = useState<string | null>(null);
  const [time, setTime] = useState(getLocalTime());

  // @TODO: 추후 options API 연동
  const options = [
    { id: '1', name: '미국 교환학생' },
    { id: '2', name: '2025 캐나다' },
    { id: '3', name: '독일 교환학생' },
  ];

  // 1분마다 한국 시간 갱신
  useEffect(() => {
    const interval = setInterval(() => {
      setTime(getLocalTime());
    }, 60_000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="sticky top-0 z-10 flex justify-between border-b border-line-solid-normal bg-background-alternative px-8 py-3">
      <Dropdown
        selected={selected}
        onSelect={setSelected}
        options={options}
      />
      <div className="flex items-center gap-5">
        <div className="flex items-center gap-2">
          <span className="label2-medium text-label-neutral">{time}</span>
          <Icons.Refresh className="h-4 w-4 cursor-pointer text-label-neutral"/>
        </div>
        <Button>모바일</Button>
        <img
          src={ProfileImage}
          alt="프로필 이미지"
          className="h-8 w-8 rounded-full object-cover cursor-pointer"
        />
      </div>
    </div>
  );
};

export default Header;
