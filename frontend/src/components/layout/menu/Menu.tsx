import { Link, useRouterState } from '@tanstack/react-router';

import MenuItem from '@/components/layout/menu/MenuItem';

import { Icons } from '@/assets';

const menuItems = [
  { to: '/home', Icon: Icons.Home, label: '홈' },
  { to: '/travel', Icon: Icons.Travel, label: '여행' },
  { to: '/report', Icon: Icons.Analytics, label: '분석' },
] as const;

const Menu = () => {
  const pathname = useRouterState({
    select: (state) => state.location.pathname,
  });

  const isInitPath = pathname.includes('/init');

  return (
    <nav className="border-line-normal-normal bg-background-normal sticky top-0 bottom-0 left-0 flex w-16 flex-col items-center gap-9 border-r px-4 py-3">
      <Link to="/home">
        <Icons.Logo className="size-8 cursor-pointer" />
      </Link>
      {!isInitPath && (
        <div className="flex flex-col gap-6">
          {menuItems.map(({ to, Icon, label }) => (
            <Link key={to} to={to}>
              <MenuItem
                logo={<Icon className="size-5" />}
                label={label}
                isActive={pathname === to}
              />
            </Link>
          ))}
        </div>
      )}
    </nav>
  );
};
export default Menu;
