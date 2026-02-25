import { useContext, useEffect, useState } from 'react';
import { useMatchRoute } from '@tanstack/react-router';
import { clsx } from 'clsx';

import Calendar, { type DateRange } from '@/components/calendar/Calendar';
import Modal, { type ModalProps } from '@/components/modal/Modal';
import { ModalContext } from '@/components/modal/useModalContext';

import { formatDateWithDay } from '@/lib/utils';

interface DatePickItemProps {
  label: string;
  date?: Date | null;
  isActive?: boolean;
}

const DatePickItem = ({ label, date, isActive }: DatePickItemProps) => {
  return (
    <div
      className={clsx(
        'rounded-modal-10 flex w-48.5 flex-col items-center gap-1 border px-6.5 py-3 transition-colors',
        isActive ? 'border-primary-normal' : 'border-line-normal-normal',
      )}
    >
      <span className="text-label-normal body2-normal-medium">{label}</span>
      {date ? (
        <span className="text-primary-normal headline2-bold">
          {formatDateWithDay(date)}
        </span>
      ) : (
        <span className="text-label-assistive headline2-bold">
          날짜를 선택해주세요
        </span>
      )}
    </div>
  );
};

export interface SelectDateContentProps extends DateRange {
  onChange: ({
    startDate,
    endDate,
  }: {
    startDate: Date | null;
    endDate: Date | null;
  }) => void;
}

export const SelectDateContent = ({
  startDate,
  endDate,
  onChange,
}: SelectDateContentProps) => {
  const context = useContext(ModalContext);
  const matchRoute = useMatchRoute();
  const isTravelRoute = !!matchRoute({ to: '/travel', fuzzy: true });
  const title = isTravelRoute ? '여행 기간 설정' : '가계부 기간 설정';

  useEffect(() => {
    const isValid = !!startDate && !!endDate;
    context?.setActionReady(isValid);
  }, [startDate, endDate, context]);

  return (
    <div className="flex flex-col gap-8">
      {/* text section */}
      <div className="flex flex-col items-center gap-2.5">
        <h2 className="text-label-normal headline1-bold">{title}</h2>
        <span className="text-label-alternative body1-normal-medium text-center">
          언제부터 기록할까요? <br />
          교환학생 지출을 기록할 기간을 선택해주세요
        </span>
      </div>

      {/* calendar section */}
      <div className="flex flex-col gap-3 px-4">
        <div className="flex items-center justify-center gap-2.5">
          <DatePickItem label="시작일" date={startDate} isActive={!startDate} />
          <DatePickItem
            label="종료일"
            date={endDate}
            isActive={!!startDate && !endDate}
          />
        </div>

        <Calendar startDate={startDate} endDate={endDate} onChange={onChange} />

        <p className="body1-normal-medium text-label-alternative pl-0.5">
          *종료일이 확실하지 않더라도 임의의 날짜를 선택해주세요. <br />
          *추후에 수정이 가능합니다.
        </p>
      </div>
    </div>
  );
};

interface SelectDateModalProps extends Omit<
  ModalProps,
  'children' | 'onAction'
> {
  initialDateRange?: DateRange;
  onConfirm: (dateRange: DateRange) => void;
  onAction?: () => void;
}

const SelectDateModal = ({
  initialDateRange,
  onConfirm,
  onAction,
  ...modalProps
}: SelectDateModalProps) => {
  const [dateRange, setDateRange] = useState<DateRange>(
    initialDateRange ?? { startDate: null, endDate: null },
  );

  useEffect(() => {
    if (modalProps.isOpen) {
      setDateRange(initialDateRange ?? { startDate: null, endDate: null });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [modalProps.isOpen]);

  const handleDatesChange = ({
    startDate,
    endDate,
  }: {
    startDate: Date | null;
    endDate: Date | null;
  }) => {
    setDateRange({ startDate, endDate });
  };

  const handleConfirm = () => {
    onConfirm(dateRange);
    onAction?.();
  };

  return (
    <Modal {...modalProps} onAction={handleConfirm}>
      <SelectDateContent
        startDate={dateRange.startDate}
        endDate={dateRange.endDate}
        onChange={handleDatesChange}
      />
    </Modal>
  );
};

export default SelectDateModal;
