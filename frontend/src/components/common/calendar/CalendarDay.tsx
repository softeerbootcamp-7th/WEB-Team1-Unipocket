import { isSameDay } from '@/components/common/calendar/date.utils';

import { cn } from '@/lib/utils';

type CalendarDayProps = {
  day: number;
  fullDate: Date;
  today: Date;
  isCurrentMonth: boolean;
  startDate: Date | null;
  endDate: Date | null;
  onDateClick: (date: Date) => void;
};

export const CalendarDay = ({
  day,
  fullDate,
  today,
  isCurrentMonth,
  startDate,
  endDate,
  onDateClick,
}: CalendarDayProps) => {
  const dayOfWeek = fullDate.getDay(); // 0: 일요일, 6: 토요일
  const isCheckIn = isSameDay(fullDate, startDate);
  const isCheckOut = isSameDay(fullDate, endDate);
  const isToday = isSameDay(fullDate, today);

  let isInRange = false;
  if (startDate && endDate) {
    const time = fullDate.getTime();
    if (time > startDate.getTime() && time < endDate.getTime()) {
      isInRange = true;
    }
  }

  const getRangeBackgroundClass = () => {
    const isRange = (isCheckIn && endDate) || isInRange || isCheckOut;

    return cn({
      'bg-primary-normal/8': isRange,
      'bg-primary-normal/8 rounded-full': !isRange && isToday,
      'rounded-l-full left-[2.4px]': isCheckIn && endDate,
      'rounded-r-full right-[2.4px]': isCheckOut,
      'rounded-l-[0px]': dayOfWeek === 0 && !isCheckIn,
      'rounded-r-[0px]': dayOfWeek === 6 && !isCheckOut,
    });
  };

  const getSelectedCircleClass = () => {
    return cn({
      'bg-primary-normal text-inverse-label rounded-full':
        isCheckIn || isCheckOut,
    });
  };

  const dayColor = cn({
    'text-primary-normal': isInRange || isToday,
    'text-label-normal': !isInRange && !isToday && isCurrentMonth,
    'text-label-disable': !isInRange && !isToday && !isCurrentMonth,
  });

  return (
    <button
      onClick={() => onDateClick(fullDate)}
      className={cn(
        'figure-body2-15-semibold relative h-9 w-13 cursor-pointer p-[2.4px] text-center',
        dayColor,
      )}
    >
      <div
        className={cn(
          'absolute inset-y-[2.4px] right-0 left-0',
          getRangeBackgroundClass(),
        )}
      />
      <span
        className={cn(
          'relative z-10 flex h-full w-full items-center justify-center',
          getSelectedCircleClass(),
        )}
      >
        {day}
      </span>
    </button>
  );
};
