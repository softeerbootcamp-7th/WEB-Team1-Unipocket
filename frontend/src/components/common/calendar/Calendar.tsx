import { useState } from 'react';

import { isSameDay } from '@/components/common/calendar/date.utils';

import Icon from '../Icon';
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
  const initialDate = startDate ?? new Date();
  const [displayMonth, setDisplayMonth] = useState(
    new Date(initialDate.getFullYear(), initialDate.getMonth(), 1),
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
      if (isSameDay(date, startDate)) {
        newStart = null;
        newEnd = null;
      } else if (date < startDate) {
        newStart = date;
        newEnd = startDate;
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
        className="text-label-normal absolute top-8 left-8 rotate-180"
        onClick={() => handleMonthChange('prev')}
      >
        <Icon iconName="ChevronForward" />
      </button>

      <CalendarView
        displayDate={displayMonth}
        startDate={startDate}
        endDate={endDate}
        onDateClick={handleDateClick}
        onDisplayDateChange={setDisplayMonth}
      />

      <button
        className="text-label-normal absolute top-8 right-8"
        onClick={() => handleMonthChange('next')}
      >
        <Icon iconName="ChevronForward" />
      </button>
    </div>
  );
};

export default Calendar;
