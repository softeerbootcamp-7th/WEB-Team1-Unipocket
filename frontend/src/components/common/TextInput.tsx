import { useState } from 'react';
import clsx from 'clsx';

import { Icons } from '@/assets';

interface TextInputProps {
  title?: string;
  placeholder: string;
  errorMessage?: string;
  isError?: boolean;
}

const TextInput = ({
  title,
  placeholder,
  errorMessage,
  isError = false,
}: TextInputProps) => {
  const [value, setValue] = useState('');
  const [isFocused, setIsFocused] = useState(false);

  const showClearButton = value.length > 0 && isFocused;

  return (
    <div className="flex w-85 flex-col gap-2">
      {title && (
        <p className="label1-normal-bold text-label-neutral">{title}</p>
      )}

      <div className="relative">
        <input
          className={clsx(
            'body2-normal-regular text-label-normal w-full rounded-xl border border-solid p-3 pr-10 outline-none',
            isError
              ? 'border-status-negative'
              : 'border-line-normal-neutral focus:border-primary-normal',
          )}
          value={value}
          placeholder={placeholder}
          autoComplete="off"
          onFocus={() => setIsFocused(true)}
          onBlur={() => setIsFocused(false)}
          onChange={(e) => setValue(e.target.value)}
        />

        {showClearButton && (
          <button
            type="button"
            onMouseDown={(e) => e.preventDefault()}
            onClick={() => setValue('')}
            className="absolute top-1/2 right-3 -translate-y-1/2"
          >
            <Icons.CloseCircle className="text-label-neutral h-5.5 w-5.5" />
          </button>
        )}

        {isError && !showClearButton && (
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
