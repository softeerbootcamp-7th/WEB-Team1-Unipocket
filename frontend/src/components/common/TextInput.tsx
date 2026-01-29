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
        <p className="label1-normal-bold text-label-neutral">
          {title}
        </p>
      )}

      <div className="relative">
        <input
          className={clsx(
            'w-full p-3 pr-10 body2-normal-regular border border-solid rounded-xl outline-none text-label-normal',
            isError
              ? 'border-status-negative'
              : 'border-line-normal-neutral focus:border-primary-normal'
          )}
          value={value}
          placeholder={placeholder}
          autoComplete="off"
          onFocus={() => setIsFocused(true)}
          onBlur={() => setIsFocused(false)}
          onChange={(e) => setValue(e.target.value)}
        />

        {isError && !showClearButton && (
            <Icons.AlertCircle className="absolute right-3 top-1/2 -translate-y-1/2 h-5.5 w-5.5" />
      )}
      </div>

      {isError && errorMessage && (
        <p className="caption1-regular text-status-negative">
          {errorMessage}
        </p>
      )}
    </div>
  );
};

export default TextInput;
