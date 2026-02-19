import Modal from '@/components/modal/Modal';
import ModalFormContent from '@/components/setting-page/modal/ModalFormContent';

import type { Card } from '@/api/cards/type';

const CardDeleteModal = ({
  card,
  isSubmitting,
  onClose,
  onConfirm,
}: {
  card: Card;
  isSubmitting: boolean;
  onClose: () => void;
  onConfirm: (cardId: number) => void;
}) => {
  return (
    <Modal
      isOpen
      onClose={onClose}
      onAction={() => onConfirm(card.userCardId)}
      confirmButton={{ label: '삭제', variant: 'danger' }}
    >
      <ModalFormContent isActionReady={!isSubmitting}>
        <div className="flex w-80 flex-col gap-2 px-2">
          <p className="heading2-bold text-label-normal">카드 삭제</p>
          <p className="body2-normal-regular text-label-assistive">
            {card?.nickName} 카드를 정말 삭제하시겠어요?
          </p>
        </div>
      </ModalFormContent>
    </Modal>
  );
};

export default CardDeleteModal;
