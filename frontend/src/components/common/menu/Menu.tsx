import { useNavigate, useRouterState } from '@tanstack/react-router';

import MenuItem from '@/components/common/menu/MenuItem';

import { Icons } from '@/assets';

const menuItems = [
  { to: '/home', Icon: Icons.Home, label: '홈' },
  { to: '/travel', Icon: Icons.Travel, label: '여행' },
  { to: '/analytics', Icon: Icons.Analytics, label: '분석' },
] as const;

const Menu = () => {
  const navigate = useNavigate();
  const pathname = useRouterState({
    select: (state) => state.location.pathname,
  });

  const isInitPath = pathname.includes('/init');

  return (
    <div className="border-line-normal-normal bg-background-normal flex h-screen w-16 flex-col items-center gap-9 border-r px-4 py-3">
      <Icons.Logo
        className="h-8 w-8 cursor-pointer"
        onClick={() => navigate({ to: '/home' })}
      />
      {!isInitPath && (
        <div className="flex flex-col gap-6">
          {menuItems.map(({ to, Icon, label }) => (
            <MenuItem
              key={to}
              logo={<Icon className="h-5 w-5" />}
              label={label}
              isActive={pathname === to}
              onClick={() => navigate({ to })}
            />
          ))}
        </div>
      )}
    </div>
  );
};
export default Menu;
