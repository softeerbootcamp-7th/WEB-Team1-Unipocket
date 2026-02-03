import { useMemo } from 'react';

import {
  dayNames,
  getCalendarDateArr,
} from '@/components/common/calendar/date.utils';

import { CalendarDay } from './CalendarDay';

type CalendarViewProps = {
  displayDate: Date;
  startDate: Date | null;
  endDate: Date | null;
  onDateClick: (date: Date) => void;
};

export const CalendarView = ({
  displayDate,
  startDate,
  endDate,
  onDateClick,
}: CalendarViewProps) => {
  const year = displayDate.getFullYear();
  const monthIndex = displayDate.getMonth();
  const month = monthIndex + 1;

  const calendarDays = useMemo(() => {
    return getCalendarDateArr(displayDate);
  }, [displayDate]);

  const today = useMemo(() => {
    const d = new Date();
    d.setHours(0, 0, 0, 0);
    return d;
  }, []);

  return (
    <section className="flex flex-col gap-4">
      <p className="text-label-normal headline1-bold text-center">{`${year}년 ${month}월`}</p>
      <div className="flex flex-col gap-1">
        {/* 요일 section */}
        <div className="flex w-91 flex-row items-center justify-around">
          {dayNames.map((dayName) => (
            <span
              key={dayName}
              className="text-label-neutral label2-medium py-[13.2px]"
            >
              {dayName}
            </span>
          ))}
        </div>
        {/* 날짜 section */}
        <div className="grid grid-cols-7 gap-y-2.5">
          {calendarDays.map((dateInfo, index) => {
            return (
              <CalendarDay
                key={`${dateInfo.date.toISOString()}-${index}`}
                day={dateInfo.day}
                fullDate={dateInfo.date}
                isCurrentMonth={dateInfo.isCurrentMonth}
                today={today}
                startDate={startDate}
                endDate={endDate}
                onDateClick={onDateClick}
              />
            );
          })}
        </div>
      </div>
    </section>
  );
};
