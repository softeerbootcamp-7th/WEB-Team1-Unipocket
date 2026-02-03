import { useState } from 'react';
import { clsx } from 'clsx';

import DropDown from '@/components/common/dropdown/Dropdown';
import Icon from '@/components/common/Icon';

const WEEK_DAYS = ['일', '월', '화', '수', '목', '금', '토'];

const isSameDay = (a: Date, b: Date) =>
  a.getFullYear() === b.getFullYear() &&
  a.getMonth() === b.getMonth() &&
  a.getDate() === b.getDate();

export default function DateTimePicker() {
  const [currentMonth, setCurrentMonth] = useState(new Date());
  const [selectedDate, setSelectedDate] = useState<Date | null>(null);

  const [hour, setHour] = useState(12);
  const [minute, setMinute] = useState(0);

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

  const selectedDateTime =
    selectedDate &&
    new Date(
      selectedDate.getFullYear(),
      selectedDate.getMonth(),
      selectedDate.getDate(),
      hour,
      minute,
    );

  console.log('Selected DateTime:', selectedDateTime?.toISOString());

  return (
    <div className="rounded-modal-10 border-line-normal-normal bg-background-normal w-65 space-y-4 border p-4">
      <div className="flex items-center justify-between">
        <Icon
          iconName="ChevronBack"
          color="text-label-normal"
          width={20}
          height={20}
          onClick={() => setCurrentMonth(new Date(year, month - 1))}
        />
        <p className="text-label-normal headline2-bold">
          {year}년 {month + 1}월
        </p>
        <Icon
          iconName="ChevronForward"
          color="text-label-normal"
          width={20}
          height={20}
          onClick={() => setCurrentMonth(new Date(year, month + 1))}
        />
      </div>

      <div className="label2-medium text-label-neutral grid grid-cols-7 text-center">
        {WEEK_DAYS.map((d) => (
          <div key={d}>{d}</div>
        ))}
      </div>

      <div className="grid grid-cols-7 gap-y-2">
        {dates.map((date, idx) => {
          const isCurrentMonth = date.getMonth() === month;
          const isSelected = selectedDate && isSameDay(date, selectedDate);

          return (
            <button
              key={idx}
              onClick={() => setSelectedDate(date)}
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
            onSelect={(value) => setHour(value)}
            options={Array.from({ length: 24 }).map((_, i) => ({
              id: i,
              name: i.toString().padStart(2, '0'),
            }))}
            size="md"
          />
        </div>
        <p className="text-gray-400">:</p>
        <div className="w-16">
          <DropDown
            selected={minute}
            onSelect={(value) => setMinute(value)}
            options={Array.from({ length: 60 }).map((_, i) => ({
              id: i,
              name: i.toString().padStart(2, '0'),
            }))}
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
