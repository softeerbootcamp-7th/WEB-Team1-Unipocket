import { useState } from 'react';

import { Icons } from '@/assets';

import { CalendarView } from './CalendarView';

export interface DateRange {
  startDate: Date | null;
  endDate: Date | null;
}

interface CalendarProps {
  startDate: Date | null;
  endDate: Date | null;
  onChange: (startDate: Date | null, endDate: Date | null) => void;
}

const Calendar = ({ startDate, endDate, onChange }: CalendarProps) => {
  const [displayMonth, setDisplayMonth] = useState(
    new Date(
      (startDate ?? new Date()).getFullYear(),
      (startDate ?? new Date()).getMonth(),
      1,
    ),
  );

  const handleMonthChange = (direction: 'prev' | 'next') => {
    const monthOffset = direction === 'prev' ? -1 : 1;
    setDisplayMonth(
      new Date(
        displayMonth.getFullYear(),
        displayMonth.getMonth() + monthOffset,
        1,
      ),
    );
  };

  const handleDateClick = (date: Date) => {
    let newStart: Date | null = null;
    let newEnd: Date | null = null;

    if (startDate && !endDate) {
      if (date.getTime() < startDate.getTime()) {
        newStart = date;
        newEnd = null;
      } else if (date.getTime() === startDate.getTime()) {
        newStart = startDate; // Keep selection if same date clicked (or could toggle off)
        newEnd = null;
      } else {
        newStart = startDate;
        newEnd = date;
      }
    } else {
      newStart = date;
      newEnd = null;
    }

    onChange(newStart, newEnd);
  };

  return (
    <div className="bg-background-normal border-line-normal-normal rounded-modal-10 relative flex h-105 w-fit flex-row items-start border px-4 py-8">
      <button
        className="text-label-normal absolute top-8 left-8"
        onClick={() => handleMonthChange('prev')}
      >
        <Icons.ChevronForward className="h-6 w-6 rotate-180" />
      </button>

      <CalendarView
        displayDate={displayMonth}
        startDate={startDate}
        endDate={endDate}
        onDateClick={handleDateClick}
      />

      <button
        className="text-label-normal absolute top-8 right-8"
        onClick={() => handleMonthChange('next')}
      >
        <Icons.ChevronForward className="h-6 w-6" />
      </button>
    </div>
  );
};

export default Calendar;
