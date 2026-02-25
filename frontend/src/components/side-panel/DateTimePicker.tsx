import { useMemo, useRef, useState } from 'react';
import { clsx } from 'clsx';
import { toast } from 'sonner';

import { useClickOutside } from '@/hooks/useClickOutside';

import CalendarMonthPopover from '@/components/calendar/CalendarMonthPopover';
import { DAY_NAMES, isSameDay } from '@/components/calendar/date.utils';
import DropDown from '@/components/common/dropdown/Dropdown';
import Icon from '@/components/common/Icon';

const hourOptions = Array.from({ length: 24 }).map((_, i) => ({
  id: i,
  name: i.toString().padStart(2, '0'),
}));

const minuteOptions = Array.from({ length: 60 }).map((_, i) => ({
  id: i,
  name: i.toString().padStart(2, '0'),
}));

const OUTSIDE_CLICK_IGNORE_SELECTOR =
  '[data-radix-popper-content-wrapper], [role="dialog"], [data-datetime-label]';

interface DateTimePickerProps {
  onDateTimeSelect: (date: Date) => void;
  onClose?: () => void;
  initialDateTime: Date | null;
  startDate?: Date | null;
  endDate?: Date | null;
}

const DateTimePicker = ({
  onDateTimeSelect,
  onClose,
  initialDateTime,
  startDate,
  endDate,
}: DateTimePickerProps) => {
  const [currentMonth, setCurrentMonth] = useState<Date>(() => {
    const baseDate = initialDateTime ?? new Date();
    return new Date(baseDate.getFullYear(), baseDate.getMonth(), 1);
  });

  const [selectedDate, setSelectedDate] = useState<Date | null>(
    initialDateTime,
  );

  const [hour, setHour] = useState(
    initialDateTime ? initialDateTime.getHours() : 0,
  );

  const [minute, setMinute] = useState(
    initialDateTime ? initialDateTime.getMinutes() : 0,
  );

  const containerRef = useRef<HTMLDivElement>(null);

  useClickOutside(
    containerRef,
    () => {
      onClose?.();
    },
    {
      ignoreSelector: OUTSIDE_CLICK_IGNORE_SELECTOR,
    },
  );

  const createDateTime = (date: Date, hour: number, minute: number) => {
    return new Date(
      date.getFullYear(),
      date.getMonth(),
      date.getDate(),
      hour,
      minute,
    );
  };

  const updateDateTime = (newHour: number, newMinute: number) => {
    if (selectedDate) {
      const completeDateTime = createDateTime(selectedDate, newHour, newMinute);
      onDateTimeSelect(completeDateTime);
    }
  };

  const isToday = (date: Date | null) => {
    if (!date) return false;
    const now = new Date();
    return (
      date.getFullYear() === now.getFullYear() &&
      date.getMonth() === now.getMonth() &&
      date.getDate() === now.getDate()
    );
  };

  const handleHourSelect = (hourValue: number) => {
    if (selectedDate && isToday(selectedDate)) {
      const candidate = createDateTime(selectedDate, hourValue, minute);
      if (candidate > new Date()) {
        toast.error('미래 날짜와 시간은 입력할 수 없어요.');
        return;
      }
    }
    setHour(hourValue);
    updateDateTime(hourValue, minute);
  };

  const handleMinuteSelect = (minuteValue: number) => {
    if (selectedDate && isToday(selectedDate)) {
      const candidate = createDateTime(selectedDate, hour, minuteValue);
      if (candidate > new Date()) {
        toast.error('미래 날짜와 시간은 입력할 수 없어요.');
        return;
      }
    }
    setMinute(minuteValue);
    updateDateTime(hour, minuteValue);
  };

  const { year, month, dates } = useMemo(() => {
    const year = currentMonth.getFullYear();
    const month = currentMonth.getMonth();

    const firstDay = new Date(year, month, 1);
    const startWeekDay = firstDay.getDay();

    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const prevMonthDays = new Date(year, month, 0).getDate();

    const dates: Date[] = [];

    // 이전 달
    for (let i = 0; i < startWeekDay; i++) {
      dates.push(
        new Date(year, month - 1, prevMonthDays - startWeekDay + 1 + i),
      );
    }

    // 이번 달
    for (let i = 1; i <= daysInMonth; i++) {
      dates.push(new Date(year, month, i));
    }

    // 다음 달
    let nextMonthDay = 1;
    while (dates.length % 7 !== 0) {
      dates.push(new Date(year, month + 1, nextMonthDay));
      nextMonthDay++;
    }
    return { year, month, dates };
  }, [currentMonth]);

  const handleDateClick = (date: Date) => {
    setSelectedDate(date);

    const completeDateTime = createDateTime(date, hour, minute);
    onDateTimeSelect(completeDateTime);

    // 다른 달 날짜 클릭 시 달 이동
    if (date.getMonth() !== month || date.getFullYear() !== year) {
      setCurrentMonth(new Date(date.getFullYear(), date.getMonth(), 1));
    }
  };

  const handleMonthChange = (direction: 'prev' | 'next') => {
    const monthOffset = direction === 'prev' ? -1 : 1;
    setCurrentMonth(new Date(year, month + monthOffset));
  };

  const todayStart = useMemo(() => {
    const d = new Date();
    d.setHours(0, 0, 0, 0);
    return d;
  }, []);

  const effectiveEnd = useMemo(() => {
    if (endDate) {
      const endDateNormalized = new Date(
        endDate.getFullYear(),
        endDate.getMonth(),
        endDate.getDate(),
      );
      return endDateNormalized < todayStart ? endDateNormalized : todayStart;
    }
    return todayStart;
  }, [endDate, todayStart]);

  const effectiveStart = useMemo(() => {
    if (startDate) {
      return new Date(
        startDate.getFullYear(),
        startDate.getMonth(),
        startDate.getDate(),
      );
    }
    return null;
  }, [startDate]);

  return (
    <div
      ref={containerRef}
      className="rounded-modal-10 border-line-normal-normal bg-background-normal w-65 space-y-4 border p-4"
    >
      <div className="flex items-center justify-between">
        <Icon
          iconName="ChevronBack"
          color="text-label-normal"
          width={20}
          height={20}
          onClick={() => handleMonthChange('prev')}
        />
        <CalendarMonthPopover
          date={currentMonth}
          onDateChange={setCurrentMonth}
          modal={false}
        />
        <Icon
          iconName="ChevronForward"
          color="text-label-normal"
          width={20}
          height={20}
          onClick={() => handleMonthChange('next')}
        />
      </div>

      <div className="label2-medium text-label-neutral grid grid-cols-7 text-center">
        {DAY_NAMES.map((d) => (
          <div key={d}>{d}</div>
        ))}
      </div>

      <div className="grid grid-cols-7 gap-y-2">
        {dates.map((date) => {
          const isCurrentMonth = date.getMonth() === month;
          const isSelected = selectedDate && isSameDay(date, selectedDate);
          const dateStart = new Date(
            date.getFullYear(),
            date.getMonth(),
            date.getDate(),
          );
          const isDisabled =
            dateStart > effectiveEnd ||
            (effectiveStart !== null && dateStart < effectiveStart);

          return (
            <button
              key={date.toISOString()}
              onClick={() => !isDisabled && handleDateClick(date)}
              disabled={isDisabled}
              className={clsx(
                'figure-body2-14-semibold mx-auto flex h-7 w-8 items-center justify-center rounded-full',
                isDisabled && 'text-label-disable cursor-not-allowed',
                !isDisabled &&
                  !isSelected &&
                  !isCurrentMonth &&
                  'text-label-alternative',
                !isDisabled &&
                  !isSelected &&
                  'hover:bg-inverse-label hover:text-primary-normal',
                isSelected && 'bg-primary-normal text-inverse-label',
              )}
            >
              {date.getDate()}
            </button>
          );
        })}
      </div>

      <div className="flex items-center justify-center gap-3">
        <div className="w-16">
          <DropDown
            selectedId={hour}
            onSelect={handleHourSelect}
            options={hourOptions}
            size="md"
            itemWidth="w-18"
          />
        </div>
        <span className="text-gray-400">:</span>
        <div className="w-16">
          <DropDown
            selectedId={minute}
            onSelect={handleMinuteSelect}
            options={minuteOptions}
            size="md"
            itemWidth="w-18"
          />
        </div>
      </div>

      <div className="flex flex-col items-center gap-1">
        <p className="text-label-neutral label1-normal-medium text-center">
          해당 기간 내에서만 선택 가능합니다.
        </p>
        {(startDate || endDate) && (
          <p className="text-label-alternative label2-medium text-center">
            가계부 기간:{' '}
            {`${startDate!.getFullYear()}.${String(startDate!.getMonth() + 1).padStart(2, '0')}.${String(startDate!.getDate()).padStart(2, '0')}`}
            {' ~ '}
            {`${endDate!.getFullYear()}.${String(endDate!.getMonth() + 1).padStart(2, '0')}.${String(endDate!.getDate()).padStart(2, '0')}`}
          </p>
        )}
      </div>
    </div>
  );
};

export default DateTimePicker;
