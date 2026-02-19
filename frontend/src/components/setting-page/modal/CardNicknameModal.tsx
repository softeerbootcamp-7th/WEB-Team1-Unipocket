import { useState } from 'react';

import TextInput from '@/components/common/TextInput';
import Modal from '@/components/modal/Modal';
import ModalFormContent from '@/components/setting-page/modal/ModalFormContent';

import type { Card } from '@/api/cards/type';

const CardNicknameModal = ({
  card,
  isSubmitting,
  onClose,
  onSubmit,
}: {
  card: Card;
  isSubmitting: boolean;
  onClose: () => void;
  onSubmit: (cardId: number, nickName: string) => void;
}) => {
  const [nickName, setNickName] = useState(card.nickName);

  const isValid = nickName.trim().length > 0;

  return (
    <Modal
      isOpen
      onClose={onClose}
      onAction={() => onSubmit(card.userCardId, nickName.trim())}
      confirmButton={{ label: '저장', variant: 'solid' }}
    >
      <ModalFormContent isActionReady={isValid && !isSubmitting}>
        <div className="flex w-90 flex-col gap-4 px-2">
          <p className="heading2-bold text-label-normal">카드 별명 수정</p>
          <TextInput
            title="별명"
            placeholder="별명을 입력하세요"
            value={nickName}
            onChange={setNickName}
            isError={!isValid}
            errorMessage="별명을 입력해주세요."
          />
        </div>
      </ModalFormContent>
    </Modal>
  );
};

export default CardNicknameModal;
