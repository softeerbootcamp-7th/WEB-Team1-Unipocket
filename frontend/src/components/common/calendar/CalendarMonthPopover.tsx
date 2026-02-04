import { useEffect, useState } from 'react';
import clsx from 'clsx';

import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover';

import Icon from '../Icon';

interface CalendarMonthViewProps {
  displayDate: Date;
  onDateChange?: (date: Date) => void;
}

const CalendarMonthView = ({
  displayDate,
  onDateChange,
}: CalendarMonthViewProps) => {
  const [viewYear, setViewYear] = useState(displayDate.getFullYear());

  useEffect(() => {
    setViewYear(displayDate.getFullYear());
  }, [displayDate]);

  const handleYearChange = (direction: 'prev' | 'next') => {
    setViewYear((prev) => prev + (direction === 'prev' ? -1 : 1));
  };

  const handleMonthClick = (monthIndex: number) => {
    if (!onDateChange) return;

    const newDate = new Date(viewYear, monthIndex, 1);
    onDateChange(newDate);
  };

  const isSelected = (monthIndex: number) => {
    return (
      displayDate.getFullYear() === viewYear &&
      displayDate.getMonth() === monthIndex
    );
  };

  return (
    <div className="bg-background-normal border-line-normal-normal rounded-modal-12 shadow-popover flex w-fit flex-col items-center gap-3 border px-3 pt-5 pb-4">
      {/* Header */}
      <div className="flex w-full items-center justify-between">
        <h3 className="text-label-normal body1-normal-bold pl-3">
          {viewYear}년
        </h3>

        <div className="flex gap-2.5">
          <button onClick={() => handleYearChange('prev')}>
            <Icon iconName="ChevronBack" color="text-label-alternative" />
          </button>

          <button onClick={() => handleYearChange('next')}>
            <Icon iconName="ChevronForward" color="text-label-alternative" />
          </button>
        </div>
      </div>

      {/* Month Grid */}
      <div className="grid w-full grid-cols-3 gap-x-2 gap-y-0.5">
        {Array.from({ length: 12 }, (_, i) => i).map((monthIndex) => (
          <button
            key={monthIndex}
            onClick={() => handleMonthClick(monthIndex)}
            className="box-border h-9 w-15 p-0.5"
          >
            <div
              className={clsx(
                'label2-medium rounded-modal-8 flex h-full w-full items-center justify-center',
                isSelected(monthIndex)
                  ? 'bg-primary-normal text-inverse-label'
                  : 'text-label-normal hover:bg-primary-normal/8 hover:text-primary-normal',
              )}
            >
              {monthIndex + 1}월
            </div>
          </button>
        ))}
      </div>
    </div>
  );
};

interface CalendarMonthPopoverProps {
  date: Date;
  onDateChange: (date: Date) => void;
}

const CalendarMonthPopover = ({
  date,
  onDateChange,
}: CalendarMonthPopoverProps) => {
  const [isOpen, setIsOpen] = useState(false);

  const handleDateChange = (newDate: Date) => {
    onDateChange(newDate);
    setIsOpen(false);
  };

  return (
    <Popover open={isOpen} onOpenChange={setIsOpen}>
      <PopoverTrigger asChild>
        <button className="text-label-normal headline1-bold flex cursor-pointer items-center gap-1.5 text-center">
          {date.getFullYear()}년 {date.getMonth() + 1}월
          <div
            className={clsx(
              'transition-transform duration-500',
              isOpen && 'rotate-180',
            )}
          >
            <Icon iconName="CaretDown" width={16} height={16} />
          </div>
        </button>
      </PopoverTrigger>
      <PopoverContent className="w-auto border-none bg-transparent pt-3 shadow-none">
        <CalendarMonthView displayDate={date} onDateChange={handleDateChange} />
      </PopoverContent>
    </Popover>
  );
};

export default CalendarMonthPopover;
