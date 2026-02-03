import { Link, useLocation } from '@tanstack/react-router';

import { Icons } from '@/assets';
import ProfileImage from '@/assets/images/profile.png';
import { cn } from '@/lib/utils';

const LandingHeader = () => {
  const { hash } = useLocation();
  const currentId = hash.replace('#', '') || 'home';

  const navItems = [
    { id: 'home', label: '홈' },
    { id: 'features', label: '기능' },
    { id: 'demo', label: '체험하기' },
  ];

  return (
    <nav className="bg-background-alternative border-fill-normal flex items-center justify-between border-b px-8 py-3">
      {/* 로고 영역 */}
      <Link to="/" hash="home">
        <Icons.LogoText className="h-8 w-25" />
      </Link>

      {/* 메뉴 영역: map으로 반복 처리 */}
      <div className="body1-normal-bold flex gap-10">
        {navItems.map((item) => (
          <Link
            key={item.id}
            to="/"
            hash={item.id}
            className={cn(
              'transition-all duration-300',
              currentId !== item.id && 'text-gray-400',
            )}
          >
            {item.label}
          </Link>
        ))}
      </div>

      {/* 프로필 영역 */}
      <img
        src={ProfileImage}
        alt="프로필"
        className="h-8 w-8 rounded-full object-cover"
      />
    </nav>
  );
};

export default LandingHeader;
