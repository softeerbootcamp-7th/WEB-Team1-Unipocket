import Modal, { type ModalButton } from '@/components/modal/Modal';
import TextContext from '@/components/modal/TextModal/TextContext';

export interface TextConfirmModalProps {
  isOpen: boolean;
  onClose: () => void;
  onAction: () => void;
  title: string;
  description?: string;
  subDescription?: string;
  confirmButton?: ModalButton;
}

const TextConfirmModal = ({
  isOpen,
  onClose,
  onAction,
  title,
  description,
  subDescription,
  confirmButton = { label: '삭제', variant: 'danger' },
}: TextConfirmModalProps) => (
  <Modal
    isOpen={isOpen}
    onClose={onClose}
    onAction={onAction}
    confirmButton={confirmButton}
  >
    <TextContext
      title={title}
      description={description}
      subDescription={subDescription}
    />
  </Modal>
);

export default TextConfirmModal;
