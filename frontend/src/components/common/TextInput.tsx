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
}

const TextInput = ({
  title,
  placeholder,
  value,
  onChange,
  errorMessage,
  isError = false,
  isDisabled = false,
}: TextInputProps) => {
  const [isFocused, setIsFocused] = useState(false);

  const showClearButton = value.length > 0 && isFocused && !isDisabled;

  return (
    <div className="flex w-full flex-col gap-2">
      {title && (
        <p className="label1-normal-bold text-label-neutral">{title}</p>
      )}

      <div className="relative">
        <input
          disabled={isDisabled}
          className={clsx(
            'body2-normal-regular w-full h-12 rounded-xl p-3 pr-10 outline-none placeholder:body1-normal-regular',
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
        <p className="caption1-regular text-status-negative">{errorMessage}</p>
      )}
    </div>
  );
};

export default TextInput;
