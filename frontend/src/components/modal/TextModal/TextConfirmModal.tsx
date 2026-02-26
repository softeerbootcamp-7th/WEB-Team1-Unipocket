import Modal, { type ModalButton } from '@/components/modal/Modal';
import TextContent from '@/components/modal/TextModal/TextContent';

export interface TextConfirmModalProps {
  isOpen: boolean;
  onClose: () => void;
  onAction: () => void;
  title: string;
  description?: string;
  subDescription?: string;
  confirmButton?: ModalButton;
  backdropClassName?: string;
}

const TextConfirmModal = ({
  isOpen,
  onClose,
  onAction,
  title,
  description,
  subDescription,
  confirmButton = { label: '삭제', variant: 'danger' },
  backdropClassName,
}: TextConfirmModalProps) => (
  <Modal
    isOpen={isOpen}
    onClose={onClose}
    onAction={onAction}
    confirmButton={confirmButton}
    backdropClassName={backdropClassName}
  >
    <TextContent
      title={title}
      description={description}
      subDescription={subDescription}
    />
  </Modal>
);

export default TextConfirmModal;
