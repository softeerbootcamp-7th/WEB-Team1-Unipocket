import type { ModalProps } from '@/components/modal/Modal';
import TextConfirmModal from '@/components/modal/TextModal/TextConfirmModal';
import TextInputModal from '@/components/modal/TextModal/TextInputModal';

import { ERROR_MESSAGE } from '@/constants/message';
import { REGEX } from '@/constants/regex';

interface TravelPocketModalProps extends Omit<
  ModalProps,
  'children' | 'onAction'
> {
  mode: 'create' | 'edit' | 'delete';
  initialName?: string;
  onAction?: (name: string) => void;
}

export const TRAVEL_POCKET_MODAL_TEXT = {
  CREATE: {
    TITLE: '포켓명을 입력해주세요',
    DESCRIPTION: '추후에 수정할 수 있습니다',
    CONFIRM: '다음',
  },
  EDIT: {
    TITLE: '포켓명을 수정해주세요',
    DESCRIPTION: '공백 포함 최대 15자 이내',
    CONFIRM: '확인',
  },
  DELETE: {
    TITLE: '선택한 포켓을 삭제하시겠습니까?',
    DESCRIPTION: '내역은 삭제되지 않습니다',
    CONFIRM: '삭제',
  },
} as const;

const validatePocketName = (val: string) => {
  if (val.length > 15) return ERROR_MESSAGE.LENGTH15;
  if (val.length > 0 && !REGEX.COMMON_TEXT.test(val))
    return ERROR_MESSAGE.INVALID_CHAR;
  return undefined;
};

const TravelPocketModal = ({
  mode,
  initialName = '',
  isOpen,
  onClose,
  onAction,
}: TravelPocketModalProps) => {
  const text =
    mode === 'create'
      ? TRAVEL_POCKET_MODAL_TEXT.CREATE
      : mode === 'edit'
        ? TRAVEL_POCKET_MODAL_TEXT.EDIT
        : TRAVEL_POCKET_MODAL_TEXT.DELETE;

  if (mode === 'delete') {
    return (
      <TextConfirmModal
        isOpen={isOpen}
        onClose={onClose}
        onAction={() => onAction?.('')}
        title={text.TITLE}
        description={text.DESCRIPTION}
        confirmButton={{ label: text.CONFIRM, variant: 'danger' }}
      />
    );
  }

  return (
    <TextInputModal
      isOpen={isOpen}
      onClose={onClose}
      onAction={(name) => onAction?.(name)}
      title={text.TITLE}
      description={text.DESCRIPTION}
      initialValue={initialName}
      validate={validatePocketName}
      confirmButton={{ label: text.CONFIRM, variant: 'solid' }}
    />
  );
};

export default TravelPocketModal;
