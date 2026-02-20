import { useState } from 'react';

import Dropdown from '@/components/common/dropdown/Dropdown';
import Modal from '@/components/modal/Modal';
import ModalFormContent from '@/components/setting-page/modal/ModalFormContent';

import type { CountryCode } from '@/data/country/countryCode';

const AccountBookCurrencyModal = ({
  countryOptions,
  currentBaseCountryCode,
  isSubmitting,
  onClose,
  onSubmit,
}: {
  countryOptions: Array<{ id: number; name: string; code: CountryCode }>;
  currentBaseCountryCode: CountryCode;
  isSubmitting: boolean;
  onClose: () => void;
  onSubmit: (baseCountryCode: CountryCode) => void;
}) => {
  const [selectedCountryId, setSelectedCountryId] = useState(
    countryOptions.find((o) => o.code === currentBaseCountryCode)?.id ?? 0,
  );

  const isValid = selectedCountryId !== null;

  return (
    <Modal
      isOpen
      onClose={onClose}
      onAction={() => {
        const selected = countryOptions.find((o) => o.id === selectedCountryId);
        if (selected) onSubmit(selected.code);
      }}
      confirmButton={{ label: '저장', variant: 'solid' }}
    >
      <ModalFormContent isActionReady={isValid && !isSubmitting}>
        <div className="flex w-90 flex-col gap-4 px-2">
          <p className="heading2-bold text-label-normal">기준 통화 변경</p>
          <div className="flex flex-col gap-2">
            <p className="label1-normal-bold text-label-neutral">
              기준 국가 선택
            </p>
            <Dropdown
              selectedId={selectedCountryId}
              onSelect={setSelectedCountryId}
              options={countryOptions}
            />
          </div>
        </div>
      </ModalFormContent>
    </Modal>
  );
};

export default AccountBookCurrencyModal;
