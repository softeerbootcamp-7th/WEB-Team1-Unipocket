import clsx from 'clsx';

interface SwitchProps {
  checked: boolean;
  onChange: (checked: boolean) => void;
}

const Switch = ({ checked, onChange }: SwitchProps) => {
  const handleClick = () => {
    onChange(!checked);
  };

  return (
    <button
      type="button"
      role="switch"
      aria-checked={checked}
      onClick={handleClick}
      className={clsx(
        'relative inline-flex h-6 w-9.75 cursor-pointer items-center rounded-full transition-colors duration-200 ease-in-out',
        checked ? 'bg-primary-normal' : 'bg-fill-strong',
      )}
    >
      <span
        className={clsx(
          'inline-block h-4.5 w-4.5 transform rounded-full bg-white shadow-sm transition-transform duration-200 ease-in-out',
          checked ? 'translate-x-4.5' : 'translate-x-0.75',
        )}
      />
    </button>
  );
};

export default Switch;
