import { useState } from 'react';

import Modal, { type ModalButton } from '@/components/modal/Modal';
import TextInputContent from '@/components/modal/TextModal/TextInputContent';

export interface TextInputModalProps {
  isOpen: boolean;
  onClose: () => void;
  onAction: (value: string) => void;
  title: string;
  description?: string;
  placeholder?: string;
  initialValue: string;
  validate?: (value: string) => string | undefined;
  confirmButton?: ModalButton;
}

const TextInputModal = ({
  isOpen,
  onClose,
  onAction,
  title,
  description,
  placeholder,
  initialValue,
  validate,
  confirmButton = { label: '확인', variant: 'solid' },
}: TextInputModalProps) => {
  const [value, setValue] = useState(initialValue);
  const [prevIsOpen, setPrevIsOpen] = useState(false);

  if (isOpen !== prevIsOpen) {
    setPrevIsOpen(isOpen);
    if (isOpen) {
      setValue(initialValue);
    }
  }

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      onAction={() => onAction(value.trim())}
      confirmButton={confirmButton}
    >
      <TextInputContent
        value={value}
        onChange={setValue}
        title={title}
        description={description}
        placeholder={placeholder}
        validate={validate}
      />
    </Modal>
  );
};

export default TextInputModal;
