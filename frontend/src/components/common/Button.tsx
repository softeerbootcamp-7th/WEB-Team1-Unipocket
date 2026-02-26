import type { ComponentProps } from 'react';
import clsx from 'clsx';

export interface ButtonProps extends ComponentProps<'button'> {
  variant?: 'outlined' | 'outlined-inverse' | 'solid' | 'danger' | 'caution';
  size?: '2xs' | 'xs' | 'sm' | 'md' | 'lg';
}

const Button = ({
  variant = 'outlined',
  size = 'sm',
  disabled = false,
  onClick,
  children,
  ref,
  className,
  ...props
}: ButtonProps) => {
  const buttonClass = clsx(
    // asChild로 사용 시 넘어온 스타일이 override 않도록 하기 위해 className 분리
    className,

    'w-fit flex items-center justify-center box-border transition-colors truncate',

    /* --- disabled --- */
    disabled && 'cursor-not-allowed text-label-assistive',

    /* --- outlined --- */
    !disabled &&
      variant === 'outlined' &&
      'border border-line-normal-neutral text-label-neutral hover:bg-fill-alternative cursor-pointer',

    disabled && variant === 'outlined' && 'border border-line-normal-neutral ',

    /* --- outlined-inverse --- */
    !disabled &&
      variant === 'outlined-inverse' &&
      'border border-line-normal-strong text-inverse-label hover:bg-fill-alternative cursor-pointer',

    disabled &&
      variant === 'outlined-inverse' &&
      'border border-line-normal-strong text-line-solid-strong',

    /* --- solid --- */
    !disabled &&
      variant === 'solid' &&
      'bg-primary-normal text-inverse-label hover:bg-primary-strong cursor-pointer',

    disabled && variant === 'solid' && 'bg-interaction-disable',

    /* --- danger --- */
    !disabled &&
      variant === 'danger' &&
      'bg-status-negative text-inverse-label hover:bg-status-negative-strong cursor-pointer',

    disabled && variant === 'danger' && 'bg-status-negative/10',

    /* --- caution --- */
    !disabled &&
      variant === 'caution' &&
      'bg-status-cautionary/95 text-inverse-label hover:bg-status-cautionary-strong cursor-pointer',

    disabled && variant === 'caution' && 'bg-status-cautionary/10',

    /* --- size --- */
    size === '2xs' && 'px-[5px] py-1 h-6 caption1-medium rounded-modal-6',
    size === 'xs' && 'px-[5px] py-1 h-[26px] label2-medium rounded-modal-6',
    size === 'sm' && 'px-[14px] py-[7px] h-8 label2-medium rounded-modal-8',
    size === 'md' && 'px-5 py-[9px] h-10 body2-normal-medium rounded-modal-10',
    size === 'lg' && 'px-7 py-3 h-12 body1-normal-bold rounded-modal-12',
  );

  return (
    <button
      ref={ref}
      className={buttonClass}
      disabled={disabled}
      onClick={onClick}
      {...props}
    >
      {children}
    </button>
  );
};

export default Button;
