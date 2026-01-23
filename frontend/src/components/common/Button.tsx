import clsx from 'clsx';

interface ButtonProps {
  variant?: 'outlined' | 'solid';
  size?: 'xs' | 'sm' | 'md' | 'lg';
  disabled?: boolean;
  label: string;
}

const Button = ({
  variant = 'outlined',
  size = 'sm',
  disabled = false,
  label,
}: ButtonProps) => {
  const buttonClass = clsx(
    'w-fit flex items-center justify-center rounded-modal-8 label2-medium transition-colors box-border',
    {
      /* --- variant: outlined --- */
      'border border-line-normal-neutral text-label-neutral hover:bg-fill-alternative cursor-pointer':
        variant === 'outlined' && !disabled,
      'border border-line-normal-neutral bg-fill-alternative text-label-disable cursor-not-allowed':
        variant === 'outlined' && disabled,

      /* --- variant: solid --- */
      'bg-primary-normal text-white hover:bg-primary-strong cursor-pointer':
        variant === 'solid' && !disabled,
      'bg-fill-disable text-label-disable cursor-not-allowed':
        variant === 'solid' && disabled,

      /* --- size --- */
      'px-2 py-1 text-xs h-[26px]': size === 'xs',
      'py-1.75 px-3.5 text-sm h-8': size === 'sm',
      'py-2.5 px-4 text-base h-10': size === 'md',
      'py-3 px-5 text-lg h-12': size === 'lg',
    },
  );

  return (
    <button className={buttonClass} disabled={disabled}>
      {label}
    </button>
  );
};

export default Button;
