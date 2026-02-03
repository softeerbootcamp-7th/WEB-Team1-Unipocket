import { useCallback, useMemo, useState } from 'react';
import { clsx } from 'clsx';

import DropDown from '@/components/common/dropdown/Dropdown';
import Icon from '@/components/common/Icon';

import { dayNames, isSameDay } from '../calendar/date.utils';

const hourOptions = Array.from({ length: 24 }).map((_, i) => ({
  id: i,
  name: i.toString().padStart(2, '0'),
}));

const minuteOptions = Array.from({ length: 60 }).map((_, i) => ({
  id: i,
  name: i.toString().padStart(2, '0'),
}));

export default function DateTimePicker() {
  const [currentMonth, setCurrentMonth] = useState(new Date());
  const [selectedDate, setSelectedDate] = useState<Date | null>(null);

  const [hour, setHour] = useState(12);
  const [minute, setMinute] = useState(0);

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

      // 다른 달 날짜 클릭 시 달 이동
      if (date.getMonth() !== month || date.getFullYear() !== year) {
        setCurrentMonth(new Date(date.getFullYear(), date.getMonth(), 1));
      }
    },
    [month, year],
  );

  const handlePrevMonth = useCallback(() => {
    setCurrentMonth(new Date(year, month - 1));
  }, [year, month]);

  const handleNextMonth = useCallback(() => {
    setCurrentMonth(new Date(year, month + 1));
  }, [year, month]);

  const selectedDateTime = useMemo(() => {
    if (!selectedDate) return null;

    return new Date(
      selectedDate.getFullYear(),
      selectedDate.getMonth(),
      selectedDate.getDate(),
      hour,
      minute,
    );
  }, [selectedDate, hour, minute]);

  return (
    <div className="rounded-modal-10 border-line-normal-normal bg-background-normal w-65 space-y-4 border p-4">
      <div className="flex items-center justify-between">
        <Icon
          iconName="ChevronBack"
          color="text-label-normal"
          width={20}
          height={20}
          onClick={handlePrevMonth}
        />
        <span className="text-label-normal headline2-bold">
          {year}년 {month + 1}월
        </span>
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
        <div className="w-16">
          <DropDown
            selected={hour}
            onSelect={setHour}
            options={hourOptions}
            size="md"
          />
        </div>
        <span className="text-gray-400">:</span>
        <div className="w-16">
          <DropDown
            selected={minute}
            onSelect={setMinute}
            options={minuteOptions}
            size="md"
          />
        </div>
      </div>

      {selectedDateTime && (
        <p className="label2-medium text-center text-gray-600">
          선택됨: {selectedDateTime.toLocaleString('ko-KR')}
        </p>
      )}
    </div>
  );
}
