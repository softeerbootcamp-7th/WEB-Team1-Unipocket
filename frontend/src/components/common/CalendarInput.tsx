import clsx from 'clsx';

import { Icons } from '@/assets';

interface CalendarInputProps {
  title: string;
  value: string;
  onClick: () => void;
  onClear: () => void;
}

const CalendarInput = ({
  title,
  value,
  onClick,
  onClear,
}: CalendarInputProps) => {
  const showClearButton = value.length > 0;

  return (
    <div className="flex flex-col gap-2 text-left">
      <p className="label1-normal-bold text-label-neutral">{title}</p>
      <div className="relative" onClick={onClick}>
        {/* 너무 길어서 개행하기 위해 clsx 사용 */}
        <input
          className={clsx(
            'bg-background-normal rounded-modal-12 px-4 py-3.25 pr-20',
            'placeholder:body1-normal-regular placeholder:text-label-assistive',
            'body2-normal-regular text-label-normal',
            'border-line-normal-neutral border border-solid',
            'focus:border-primary-normal/43 focus:border-2',
          )}
          placeholder="날짜를 입력해주세요."
          value={value}
        />

        {showClearButton && (
          <Icons.CloseCircle
            className="text-label-neutral absolute top-1/2 right-11 size-5.5 -translate-y-1/2 cursor-pointer"
            onMouseDown={(e) => e.preventDefault()}
            onClick={onClear}
          />
        )}
        <Icons.Calendar className="text-label-alternative absolute top-1/2 right-3 size-6 -translate-y-1/2" />
      </div>
    </div>
  );
};

export default CalendarInput;
