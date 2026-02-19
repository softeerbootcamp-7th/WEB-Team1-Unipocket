import { useMemo, useState } from 'react';

import Dropdown from '@/components/common/dropdown/Dropdown';
import Modal from '@/components/modal/Modal';
import ModalFormContent from '@/components/setting-page/modal/ModalFormContent';

import type { CountryCode } from '@/data/countryCode';
import countryData from '@/data/countryData.json';

const AccountBookCreateModal = ({
  isSubmitting,
  onClose,
  onSubmit,
}: {
  isSubmitting: boolean;
  onClose: () => void;
  onSubmit: (data: {
    localCountryCode: CountryCode;
    startDate: string;
    endDate: string;
  }) => void;
}) => {
  const countryOptions = useMemo(
    () =>
      Object.entries(countryData).map(([code, data], index) => ({
        id: index + 1,
        name: data.countryName,
        code: code as CountryCode,
      })),
    [],
  );

  const [selectedCountryId, setSelectedCountryId] = useState<number | null>(
    countryOptions[0]?.id ?? null,
  );
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  const selectedCountry = countryOptions.find(
    (option) => option.id === selectedCountryId,
  );

  const isValid =
    !!selectedCountry &&
    startDate.trim().length > 0 &&
    endDate.trim().length > 0;

  return (
    <Modal
      isOpen
      onClose={onClose}
      onAction={() =>
        selectedCountry &&
        onSubmit({
          localCountryCode: selectedCountry.code,
          startDate,
          endDate,
        })
      }
      confirmButton={{ label: '생성', variant: 'solid' }}
    >
      <ModalFormContent isActionReady={isValid && !isSubmitting}>
        <div className="flex w-90 flex-col gap-4 px-2">
          <p className="heading2-bold text-label-normal">새 가계부 만들기</p>
          <div className="flex flex-col gap-2">
            <p className="label1-normal-bold text-label-neutral">국가 선택</p>
            <Dropdown
              selected={selectedCountryId}
              onSelect={setSelectedCountryId}
              options={countryOptions}
            />
          </div>
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

export default AccountBookCreateModal;
