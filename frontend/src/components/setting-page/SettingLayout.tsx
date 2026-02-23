import type { ComponentPropsWithoutRef } from 'react';
import clsx from 'clsx';

const SettingSection = ({ children }: { children: React.ReactNode }) => {
  return <div className="flex py-2.5">{children}</div>;
};

interface SettingTitleProps extends ComponentPropsWithoutRef<'h1'> {
  disabled?: boolean;
}

const SettingTitle = ({ children, disabled = false }: SettingTitleProps) => {
  return (
    <h1
      className={clsx('heading2-bold w-50', {
        'text-label-assistive': disabled,
        'text-label-normal': !disabled,
      })}
    >
      {children}
    </h1>
  );
};

interface SettingRowProps {
  label: string;
  value: string;
  onEdit: () => void;
}

const SettingRow = ({ label, value, onEdit }: SettingRowProps) => {
  return (
    <div className="border-line-normal-neutral flex items-center border-b py-3">
      <span className="label1-normal-bold text-label-neutral w-40 shrink-0">
        {label}
      </span>
      <span className="body2-normal-regular text-label-normal flex-1">
        {value}
      </span>
      <button
        onClick={onEdit}
        className="caption1-regular text-primary-normal shrink-0"
      >
        수정
      </button>
    </div>
  );
};

export { SettingRow, SettingSection, SettingTitle };
