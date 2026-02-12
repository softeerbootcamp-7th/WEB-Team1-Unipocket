import type { ComponentProps } from 'react';
import clsx from 'clsx';

import { Icons } from '@/assets';

interface FilterProps extends ComponentProps<'button'> {
  size?: 'xs' | 'sm' | 'md' | 'lg';
  disabled?: boolean;
  active?: boolean;
  isOpen?: boolean;
  onReset?: () => void;
}

const Filter = ({
  size = 'sm',
  disabled = false,
  active = false,
  isOpen = false,
  onClick,
  onReset,
  children,
  ref,
  className,
  ...props
}: FilterProps) => {
  const FilterClass = clsx(
    // asChild로 사용 시 넘어온 스타일이 override 않도록 하기 위해 className 분리
    className,

    'w-fit flex items-center justify-between gap-0.5 box-border transition-colors',
    'border cursor-pointer',

    /* --- disabled --- */
    disabled &&
      'border-line-normal-normal text-label-disable cursor-not-allowed',

    /* --- active --- */
    !disabled &&
      active &&
      'border-primary-normal text-primary-normal hover:bg-primary-normal/5',

    !disabled && active && isOpen && 'bg-primary-normal/10',

    /* --- inactive --- */
    !disabled &&
      !active &&
      'border-line-normal-normal text-label-neutral hover:bg-fill-alternative',

    !disabled && !active && isOpen && 'bg-fill-strong',

    /* --- size --- */
    size === 'xs' &&
      'pl-[7px] pr-[5px] py-1 h-6 caption1-medium rounded-modal-6',
    size === 'sm' &&
      'pl-2 pr-[6px] py-[6px] h-8 label1-normal-medium rounded-modal-8',
    size === 'md' &&
      'pl-[11px] pr-[9px] py-[7px] h-9 body2-normal-medium rounded-modal-10',
    size === 'lg' &&
      'pl-[11px] pr-[9px] py-[7px] h-[50px] body2-normal-medium rounded-modal-10',
  );

  return (
    <button
      className={FilterClass}
      disabled={disabled}
      onClick={onClick}
      ref={ref}
      {...props}
    >
      <span className="px-0.5 whitespace-nowrap">{children}</span>
      {active ? (
        <div
          onClick={(e) => {
            e.stopPropagation();
            onReset?.();
          }}
        >
          <Icons.Close className="size-3.5" />
        </div>
      ) : (
        <Icons.CaretDown
          className={clsx(
            'size-2 transition-transform duration-300',
            isOpen && 'rotate-180',
          )}
        />
      )}
    </button>
  );
};

export default Filter;
