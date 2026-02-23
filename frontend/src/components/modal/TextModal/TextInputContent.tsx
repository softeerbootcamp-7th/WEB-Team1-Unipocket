import type React from 'react';
import { useEffect } from 'react';

import TextInput from '@/components/common/TextInput';
import { useModalContext } from '@/components/modal/useModalContext';

interface TextInputContentProps {
  value: string;
  onChange: (value: string) => void;
  title: string;
  description?: string;
  placeholder?: string;
  validate?: (value: string) => string | undefined;
}

const TextInputContent = ({
  value,
  onChange,
  title,
  description,
  placeholder = '텍스트를 입력해 주세요.',
  validate,
}: TextInputContentProps) => {
  const context = useModalContext();

  const errorMessage = validate ? validate(value) : undefined;

  const isValid = value.length > 0 && errorMessage === undefined;

  useEffect(() => {
    context.setActionReady(isValid);
  }, [isValid, context]);

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && isValid) {
      context.onAction();
    }
  };

  return (
    <div className="flex h-60.5 w-83.75 flex-col gap-13">
      <div className="flex flex-col items-center gap-2.5">
        <h2 className="text-label-normal headline1-bold">{title}</h2>
        {description && (
          <span className="text-label-alternative body1-normal-medium text-center">
            {description}
          </span>
        )}
      </div>

      <div className="px-4">
        <TextInput
          placeholder={placeholder}
          value={value}
          onChange={onChange}
          onKeyDown={handleKeyDown}
          isError={!!errorMessage}
          errorMessage={errorMessage}
        />
      </div>
    </div>
  );
};

export default TextInputContent;
