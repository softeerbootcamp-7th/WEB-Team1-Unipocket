import clsx from 'clsx';

interface MenuItemProps {
  logo: React.ReactNode;
  label: string;
  isActive: boolean;
}

const MenuItem = ({ logo, label, isActive }: MenuItemProps) => {
  return (
    <button
      type="button"
      className={clsx(
        'flex w-8 cursor-pointer flex-col items-center gap-0.5',
        isActive ? 'text-label-neutral' : 'text-label-alternative',
      )}
    >
      <div
        className={clsx(
          'flex h-7 w-7 items-center justify-center px-1',
          isActive ? 'bg-fill-strong rounded-sm' : '',
        )}
      >
        {logo}
      </div>
      <div className="caption1-medium text-center">{label}</div>
    </button>
  );
};

export default MenuItem;
