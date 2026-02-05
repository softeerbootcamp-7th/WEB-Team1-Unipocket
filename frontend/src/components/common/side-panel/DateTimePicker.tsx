import { useCallback, useMemo, useRef, useState } from 'react';
import { clsx } from 'clsx';

import { useOutsideClick } from '@/hooks/useOutsideClick';

import DropDown from '@/components/common/dropdown/Dropdown';
import Icon from '@/components/common/Icon';

import CalendarMonthPopover from '../calendar/CalendarMonthPopover';
import { dayNames, isSameDay } from '../calendar/date.utils';

const hourOptions = Array.from({ length: 24 }).map((_, i) => ({
  id: i,
  name: i.toString().padStart(2, '0'),
}));

const minuteOptions = Array.from({ length: 60 }).map((_, i) => ({
  id: i,
  name: i.toString().padStart(2, '0'),
}));

export default function DateTimePicker({
  onDateTimeSelect,
  onClose,
  initialDateTime,
}: {
  onDateTimeSelect?: (date: Date) => void;
  onClose?: () => void;
  initialDateTime?: Date | null;
}) {
  const [currentMonth, setCurrentMonth] = useState<Date>(
    initialDateTime ? new Date(initialDateTime.getFullYear(), initialDateTime.getMonth(), 1) : new Date()
  );
  const [selectedDate, setSelectedDate] = useState<Date | null>(initialDateTime || null);

  const [hour, setHour] = useState(initialDateTime?.getHours() ?? 0);
  const [minute, setMinute] = useState(initialDateTime?.getMinutes() ?? 0);

  const containerRef = useRef<HTMLDivElement>(null);

  useOutsideClick(containerRef, () => {
    onClose?.();
  });

  const updateDateTime = (newHour: number, newMinute: number) => {
    if (selectedDate) {
      const completeDateTime = new Date(
        selectedDate.getFullYear(),
        selectedDate.getMonth(),
        selectedDate.getDate(),
        newHour,
        newMinute,
      );
      onDateTimeSelect?.(completeDateTime);
    }
  };

  const handleHourSelect = (hourValue: number) => {
    setHour(hourValue);
    updateDateTime(hourValue, minute);
  };

  const handleMinuteSelect = (minuteValue: number) => {
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

  const handleDateClick = useCallback(
    (date: Date) => {
      setSelectedDate(date);

      const completeDateTime = new Date(
        date.getFullYear(),
        date.getMonth(),
        date.getDate(),
        hour,
        minute,
      );
      onDateTimeSelect?.(completeDateTime);

      // 다른 달 날짜 클릭 시 달 이동
      if (date.getMonth() !== month || date.getFullYear() !== year) {
        setCurrentMonth(new Date(date.getFullYear(), date.getMonth(), 1));
      }
    },
    [hour, minute, month, onDateTimeSelect, year],
  );

  const handlePrevMonth = useCallback(() => {
    setCurrentMonth(new Date(year, month - 1));
  }, [year, month]);

  const handleNextMonth = useCallback(() => {
    setCurrentMonth(new Date(year, month + 1));
  }, [year, month]);

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
          onClick={handlePrevMonth}
        />
        <CalendarMonthPopover
          date={currentMonth}
          onDateChange={setCurrentMonth}
        />
        <Icon
          iconName="ChevronForward"
          color="text-label-normal"
          width={20}
          height={20}
          onClick={handleNextMonth}
        />
      </div>

      <div className="label2-medium text-label-neutral grid grid-cols-7 text-center">
        {dayNames.map((d) => (
          <div key={d}>{d}</div>
        ))}
      </div>

      <div className="grid grid-cols-7 gap-y-2">
        {dates.map((date) => {
          const isCurrentMonth = date.getMonth() === month;
          const isSelected = selectedDate && isSameDay(date, selectedDate);

          return (
            <button
              key={date.toISOString()}
              onClick={() => handleDateClick(date)}
              className={clsx(
                'figure-body2-14-semibold mx-auto flex h-7 w-8 items-center justify-center rounded-full',
                !isCurrentMonth && !isSelected && 'text-label-disable',
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
        <div className='w-16'>
          <DropDown
            selected={hour}
            onSelect={handleHourSelect}
            options={hourOptions}
            size="md"
          />
        </div>
        <span className="text-gray-400">:</span>
        <div className='w-16'>
          <DropDown
            selected={minute}
            onSelect={handleMinuteSelect}
            options={minuteOptions}
            size="md"
          />
        </div>
      </div>
    </div>
  );
}
