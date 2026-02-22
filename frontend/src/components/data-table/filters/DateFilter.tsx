import { useState } from 'react';

import Calendar from '@/components/calendar/Calendar';
import {
  DATE_PRESETS,
  type DatePresetId,
  getDateRangeFromPreset,
} from '@/components/calendar/date.utils';
import CalendarInput from '@/components/common/CalendarInput';
import Divider from '@/components/common/Divider';
import Filter from '@/components/common/Filter';
import { useDataTableFilter } from '@/components/data-table/context';
import {
  Popover,
  PopoverAnchor,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover';

import { formatDateToString, toOffsetDateTimeString } from '@/lib/utils';

const DateFilter = () => {
  const { filter, updateFilter } = useDataTableFilter();

  const [isFilterOpen, setIsFilterOpen] = useState(false);
  const [isCalendarOpen, setIsCalendarOpen] = useState(false);

  const isActive = !!(filter.startDate || filter.endDate);

  const handleDateChange = ({
    startDate,
    endDate,
  }: {
    startDate: Date | null;
    endDate: Date | null;
  }) => {
    updateFilter({
      startDate: startDate
        ? toOffsetDateTimeString(startDate, false)
        : undefined, // 00:00:00
      endDate: endDate ? toOffsetDateTimeString(endDate, true) : undefined, // 23:59:59
    });
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
    const startStr = filter.startDate
      ? formatDateToString(new Date(filter.startDate))
      : '';
    const endStr = filter.endDate
      ? formatDateToString(new Date(filter.endDate))
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
                    filter.startDate
                      ? formatDateToString(new Date(filter.startDate))
                      : ''
                  }
                  onClick={() => setIsCalendarOpen(true)}
                  onClear={() =>
                    handleDateChange({
                      startDate: null,
                      endDate: filter.endDate ? new Date(filter.endDate) : null,
                    })
                  }
                />
                <CalendarInput
                  title="종료일"
                  value={
                    filter.endDate
                      ? formatDateToString(new Date(filter.endDate))
                      : ''
                  }
                  onClick={() => setIsCalendarOpen(true)}
                  onClear={() =>
                    handleDateChange({
                      startDate: filter.startDate
                        ? new Date(filter.startDate)
                        : null,
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
                startDate={filter.startDate ? new Date(filter.startDate) : null}
                endDate={filter.endDate ? new Date(filter.endDate) : null}
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
