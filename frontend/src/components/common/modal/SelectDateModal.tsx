import { useEffect, useState } from 'react';

import Calendar, {
  type DateRange,
} from '@/components/common/calendar/Calendar';

import { formatDateWithDay } from '@/lib/utils';

import Modal, { type ModalProps } from './Modal';
import { useModalContext } from './useModalContext';

interface DatePickItemProps {
  label: string;
  date?: Date | null;
}

const DatePickItem = ({ label, date }: DatePickItemProps) => {
  const borderColor = date
    ? 'border-primary-normal'
    : 'border-line-normal-normal';
  return (
    <div
      className={`rounded-modal-10 flex w-48.5 flex-col items-center gap-1 border px-6.5 py-3 transition-colors ${borderColor}`}
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

interface SelectDateContentProps extends DateRange {
  onChange: (startDate: Date | null, endDate: Date | null) => void;
}

const SelectDateContent = ({
  startDate,
  endDate,
  onChange,
}: SelectDateContentProps) => {
  const { setActionReady } = useModalContext();

  // 여기서 useEffect로 버튼 제어
  useEffect(() => {
    setActionReady(!!startDate && !!endDate);
  }, [startDate, endDate, setActionReady]);

  return (
    <div className="flex flex-col gap-8">
      {/* text section */}
      <div className="flex flex-col items-center gap-2.5">
        <h2 className="text-label-normal headline1-bold">
          카드 연동 기간 설정
        </h2>
        <span className="text-label-alternative body1-normal-medium text-center">
          언제부터 카드 내역을 불러올까요? <br />
          교환학생 지출을 기록할 기간을 선택해주세요
        </span>
      </div>

      {/* calendar section */}
      <div className="flex flex-col gap-3 px-4">
        <div className="flex items-center justify-center gap-2.5">
          <DatePickItem label="시작일" date={startDate} />
          <DatePickItem label="종료일" date={endDate} />
        </div>

        <Calendar startDate={startDate} endDate={endDate} onChange={onChange} />
      </div>
    </div>
  );
};

interface SelectDateModalProps extends Omit<ModalProps, 'children'> {
  initialDateRange?: DateRange;
}

const SelectDateModal = ({
  initialDateRange,
  ...modalProps
}: SelectDateModalProps) => {
  const [startDate, setStartDate] = useState<Date | null>(
    initialDateRange?.startDate ?? null,
  );
  const [endDate, setEndDate] = useState<Date | null>(
    initialDateRange?.endDate ?? null,
  );

  const handleDatesChange = (
    newStartDate: Date | null,
    newEndDate: Date | null,
  ) => {
    setStartDate(newStartDate);
    setEndDate(newEndDate);
  };

  return (
    <Modal {...modalProps}>
      <SelectDateContent
        startDate={startDate}
        endDate={endDate}
        onChange={handleDatesChange}
      />
    </Modal>
  );
};

export default SelectDateModal;
