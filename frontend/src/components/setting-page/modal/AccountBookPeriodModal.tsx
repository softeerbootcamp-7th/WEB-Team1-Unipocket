import { useState } from 'react';

import Modal from '@/components/modal/Modal';
import ModalFormContent from '@/components/setting-page/modal/ModalFormContent';

const AccountBookPeriodModal = ({
  currentStartDate,
  currentEndDate,
  isSubmitting,
  onClose,
  onSubmit,
}: {
  currentStartDate: string;
  currentEndDate: string;
  isSubmitting: boolean;
  onClose: () => void;
  onSubmit: (startDate: string, endDate: string) => void;
}) => {
  const [startDate, setStartDate] = useState(currentStartDate);
  const [endDate, setEndDate] = useState(currentEndDate);

  const isValid = startDate.trim().length > 0 && endDate.trim().length > 0;

  return (
    <Modal
      isOpen
      onClose={onClose}
      onAction={() => onSubmit(startDate, endDate)}
      confirmButton={{ label: '저장', variant: 'solid' }}
    >
      <ModalFormContent isActionReady={isValid && !isSubmitting}>
        <div className="flex w-90 flex-col gap-4 px-2">
          <p className="heading2-bold text-label-normal">카드 연동 기간 변경</p>
          <div className="flex flex-col gap-2">
            <p className="label1-normal-bold text-label-neutral">기간 설정</p>
            <div className="flex flex-wrap gap-3">
              <input
                type="date"
                className="bg-background-normal body2-normal-regular border-line-normal-neutral h-12 rounded-xl border px-3"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
              />
              <input
                type="date"
                className="bg-background-normal body2-normal-regular border-line-normal-neutral h-12 rounded-xl border px-3"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
              />
            </div>
          </div>
        </div>
      </ModalFormContent>
    </Modal>
  );
};

export default AccountBookPeriodModal;
