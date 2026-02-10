import { useState } from 'react';
import clsx from 'clsx';

import { Icons } from '@/assets';

interface TextInputProps {
  title?: string;
  placeholder: string;
  value: string;
  onChange: (value: string) => void;
  errorMessage?: string;
  isError?: boolean;
  isDisabled?: boolean;
  className?: string;
  prefix?: React.ReactNode;
}

const TextInput = ({
  title,
  placeholder,
  value,
  onChange,
  errorMessage,
  isError = false,
  isDisabled = false,
  className,
  prefix,
}: TextInputProps) => {
  const [isFocused, setIsFocused] = useState(false);

  const showClearButton = value.length > 0 && isFocused && !isDisabled;

  return (
    <div className={clsx('flex flex-col gap-2 text-left', className)}>
      {title && (
        <p className="label1-normal-bold text-label-neutral">{title}</p>
      )}

      <div className="relative">
        {prefix && (
          <div className="body1-normal-medium text-label-assistive pointer-events-none absolute top-1/2 left-3 -translate-y-1/2 p-1">
            {prefix}
          </div>
        )}
        <input
          disabled={isDisabled}
          className={clsx(
            'bg-background-normal body2-normal-regular placeholder:body1-normal-regular placeholder:text-label-assistive h-12 w-full rounded-xl p-3 pr-10 outline-none',
            prefix ? 'pl-10.5' : 'pl-3',
            isDisabled
              ? 'bg-interaction-disable text-label-disable cursor-not-allowed'
              : 'text-label-normal border border-solid focus:border-2',
            isError
              ? 'border-status-negative/43'
              : 'border-line-normal-neutral focus:border-primary-normal/43',
          )}
          value={value}
          placeholder={placeholder}
          autoComplete="off"
          onFocus={() => !isDisabled && setIsFocused(true)}
          onBlur={() => setIsFocused(false)}
          onChange={(e) => !isDisabled && onChange(e.target.value)}
        />

        {!isDisabled && showClearButton && (
          <button
            type="button"
            onMouseDown={(e) => e.preventDefault()}
            onClick={() => onChange('')}
            className="absolute top-1/2 right-3 -translate-y-1/2"
          >
            <Icons.CloseCircle className="text-label-neutral h-5.5 w-5.5" />
          </button>
        )}

        {isError && !isDisabled && !showClearButton && (
          <Icons.AlertCircle className="absolute top-1/2 right-3 h-5.5 w-5.5 -translate-y-1/2" />
        )}
      </div>

      {isError && errorMessage && (
        <p className="caption1-regular text-status-negative -mt-0.5 -mb-5">
          {errorMessage}
        </p>
      )}
    </div>
  );
};

export default TextInput;
