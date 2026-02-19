import { useState } from 'react';

import TextInput from '@/components/common/TextInput';
import Modal from '@/components/modal/Modal';
import ModalFormContent from '@/components/setting-page/modal/ModalFormContent';

const CardCreateModal = ({
  onClose,
  onSubmit,
  isSubmitting,
}: {
  onClose: () => void;
  onSubmit: (data: {
    cardCompany: string;
    cardNumber: string;
    nickName: string;
  }) => void;
  isSubmitting: boolean;
}) => {
  const [cardCompany, setCardCompany] = useState('');
  const [cardNumber, setCardNumber] = useState('');
  const [nickName, setNickName] = useState('');

  const isValid =
    cardCompany.trim().length > 0 &&
    cardNumber.trim().length > 0 &&
    nickName.trim().length > 0;

  return (
    <Modal
      isOpen
      onClose={onClose}
      onAction={() =>
        onSubmit({
          cardCompany: cardCompany.trim(),
          cardNumber: cardNumber.trim(),
          nickName: nickName.trim(),
        })
      }
      confirmButton={{ label: '등록', variant: 'solid' }}
    >
      <ModalFormContent isActionReady={isValid && !isSubmitting}>
        <div className="flex w-90 flex-col gap-4 px-2">
          <p className="heading2-bold text-label-normal">카드 등록</p>
          <TextInput
            title="카드사"
            placeholder="SHINHAN"
            value={cardCompany}
            onChange={setCardCompany}
            isError={cardCompany.trim().length === 0}
            errorMessage="카드사를 입력해주세요."
          />
          <TextInput
            title="카드 번호"
            placeholder="1433"
            value={cardNumber}
            onChange={setCardNumber}
            isError={cardNumber.trim().length === 0}
            errorMessage="카드 번호를 입력해주세요."
          />
          <TextInput
            title="별명"
            placeholder="별명을 입력하세요"
            value={nickName}
            onChange={setNickName}
            isError={nickName.trim().length === 0}
            errorMessage="별명을 입력해주세요."
          />
        </div>
      </ModalFormContent>
    </Modal>
  );
};

export default CardCreateModal;
