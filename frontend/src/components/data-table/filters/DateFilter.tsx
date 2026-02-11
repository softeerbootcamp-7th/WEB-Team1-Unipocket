import { useState } from 'react';

import Calendar, { type DateRange } from '@/components/calendar/Calendar';
import CalendarInput from '@/components/common/CalendarInput';
import Divider from '@/components/common/Divider';
import Filter from '@/components/common/Filter';
import {
  Popover,
  PopoverAnchor,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover';

import { formatDateToString } from '@/lib/utils';

import {
  DATE_PRESETS,
  type DatePresetId,
  getDateRangeFromPreset,
} from '../../calendar/date.utils';

const DateFilter = () => {
  const [dateRange, setDateRange] = useState<DateRange>({
    startDate: null,
    endDate: null,
  });
  const [isFilterOpen, setIsFilterOpen] = useState(false);
  const [isCalendarOpen, setIsCalendarOpen] = useState(false);

  const isActive = !!(dateRange.startDate || dateRange.endDate);

  const handleDateChange = ({
    startDate,
    endDate,
  }: {
    startDate: Date | null;
    endDate: Date | null;
  }) => {
    setDateRange({ startDate, endDate });
  };

  const handlePresetClick = (id: DatePresetId) => {
    const { startDate, endDate } = getDateRangeFromPreset(id);
    handleDateChange({ startDate, endDate });
  };

  const handleReset = () => {
    handleDateChange({ startDate: null, endDate: null });
  };

  const filterLabel = (() => {
    if (!isActive) return '날짜';
    const startStr = dateRange.startDate
      ? formatDateToString(dateRange.startDate)
      : '';
    const endStr = dateRange.endDate
      ? formatDateToString(dateRange.endDate)
      : '';
    return `날짜: ${startStr} - ${endStr}`;
  })();

  return (
    <Popover open={isFilterOpen} onOpenChange={setIsFilterOpen} modal={false}>
      <PopoverTrigger asChild>
        <Filter
          size="md"
          isOpen={isFilterOpen}
          active={isActive}
          onReset={handleReset}
        >
          {filterLabel}
        </Filter>
      </PopoverTrigger>

      <PopoverContent align="start" sideOffset={12} asChild>
        <div className="bg-background-normal shadow-semantic-subtle rounded-modal-12 flex p-4">
          {/* 프리셋 영역 */}
          <div className="flex min-w-25 flex-col">
            <p className="headline2-bold text-label-neutral mb-4 text-left">
              기간 선택
            </p>
            {DATE_PRESETS.map((preset) => (
              <button
                key={preset.id}
                onClick={() => handlePresetClick(preset.id)}
                className="label1-normal-medium text-label-normal w-full cursor-pointer rounded-sm p-2 text-left"
              >
                {preset.label}
              </button>
            ))}
          </div>

          <Divider style="vertical" className="mx-6" />

          {/* 달력 및 인풋 영역 */}
          <Popover
            open={isCalendarOpen}
            onOpenChange={setIsCalendarOpen}
            modal={false}
          >
            <PopoverAnchor asChild>
              <div className="flex flex-col justify-between">
                <CalendarInput
                  title="시작일"
                  value={
                    dateRange.startDate
                      ? formatDateToString(dateRange.startDate)
                      : ''
                  }
                  onClick={() => setIsCalendarOpen(true)}
                  onClear={() =>
                    handleDateChange({
                      startDate: null,
                      endDate: dateRange.endDate,
                    })
                  }
                />
                <CalendarInput
                  title="종료일"
                  value={
                    dateRange.endDate
                      ? formatDateToString(dateRange.endDate)
                      : ''
                  }
                  onClick={() => setIsCalendarOpen(true)}
                  onClear={() =>
                    handleDateChange({
                      startDate: dateRange.startDate,
                      endDate: null,
                    })
                  }
                />
              </div>
            </PopoverAnchor>

            <PopoverContent
              align="start"
              side="right"
              sideOffset={20}
              alignOffset={-20}
            >
              <Calendar
                startDate={dateRange.startDate}
                endDate={dateRange.endDate}
                onChange={handleDateChange}
              />
            </PopoverContent>
          </Popover>
        </div>
      </PopoverContent>
    </Popover>
  );
};

export default DateFilter;
