import { useEffect, useState } from 'react';

import type { ModalProps } from '@/components/modal/Modal';
import Modal from '@/components/modal/Modal';
import TextContext from '@/components/modal/TextModal/TextContext';
import TextInputContent from '@/components/modal/TextModal/TextInputContent';

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
    DESCRIPTION: '공백 포함 최대 13자 이내',
    CONFIRM: '확인',
  },
  DELETE: {
    TITLE: '선택한 포켓을 삭제하시겠습니까?',
    DESCRIPTION: '내역은 삭제되지 않습니다',
    CONFIRM: '삭제',
  },
} as const;

const TravelPocketModal = ({
  mode,
  initialName = '',
  onClose,
  onAction,
  ...modalProps
}: TravelPocketModalProps) => {
  const [pocketName, setPocketName] = useState(initialName);

  useEffect(() => {
    if (modalProps.isOpen) {
      setPocketName(initialName);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [modalProps.isOpen]);

  const validatePocketName = (val: string) => {
    if (val.length > 13) return ERROR_MESSAGE.LENGTH13;
    if (val.length > 0 && !REGEX.COMMON_TEXT.test(val))
      return ERROR_MESSAGE.INVALID_CHAR;
    return undefined;
  };

  const text =
    mode === 'create'
      ? TRAVEL_POCKET_MODAL_TEXT.CREATE
      : mode === 'edit'
        ? TRAVEL_POCKET_MODAL_TEXT.EDIT
        : TRAVEL_POCKET_MODAL_TEXT.DELETE;

  const isCreate = mode === 'create';
  const isEdit = mode === 'edit';
  const isDelete = mode === 'delete';

  const handleAction = async () => {
    if (isCreate) {
      console.log('생성 API 호출:', pocketName);
    } else if (isEdit) {
      console.log('수정 API 호출:', pocketName);
    } else if (isDelete) {
      console.log('삭제 API 호출:', pocketName);
    }

    onAction?.(pocketName);
  };

  return (
    <Modal
      {...modalProps}
      onClose={onClose}
      onAction={handleAction}
      confirmButton={{
        label: text.CONFIRM,
        variant: isDelete ? 'danger' : 'solid',
      }}
    >
      {isCreate || isEdit ? (
        <TextInputContent
          value={pocketName}
          onChange={setPocketName}
          title={text.TITLE}
          description={text.DESCRIPTION}
          validate={validatePocketName}
        />
      ) : (
        <TextContext title={text.TITLE} description={text.DESCRIPTION} />
      )}
    </Modal>
  );
};

export default TravelPocketModal;
