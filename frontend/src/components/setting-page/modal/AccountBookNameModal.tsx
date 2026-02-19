import { useState } from 'react';

import TextInput from '@/components/common/TextInput';
import Modal from '@/components/modal/Modal';
import ModalFormContent from '@/components/setting-page/modal/ModalFormContent';

import type { AccountBookSummary } from '@/api/account-books/type';

const AccountBookNameModal = ({
  accountBooks,
  accountBookId,
  currentTitle,
  isSubmitting,
  onClose,
  onSubmit,
}: {
  accountBooks: AccountBookSummary[];
  accountBookId: number | null;
  currentTitle: string;
  isSubmitting: boolean;
  onClose: () => void;
  onSubmit: (title: string) => void;
}) => {
  const [title, setTitle] = useState(currentTitle);

  const isDuplicate = accountBooks.some(
    (book) => book.id !== accountBookId && book.title === title.trim(),
  );
  const isTooLong = title.trim().length > 10;
  const isValid = title.trim().length > 0 && !isDuplicate && !isTooLong;

  const errorMessage = isTooLong
    ? '최대 10자까지 입력할 수 있어요.'
    : isDuplicate
      ? '이미 동일한 이름의 가계부가 있어요.'
      : title.trim().length === 0
        ? '이름을 입력해주세요.'
        : '';

  return (
    <Modal
      isOpen
      onClose={onClose}
      onAction={() => onSubmit(title.trim())}
      confirmButton={{ label: '저장', variant: 'solid' }}
    >
      <ModalFormContent isActionReady={isValid && !isSubmitting}>
        <div className="flex w-90 flex-col gap-4 px-2">
          <p className="heading2-bold text-label-normal">가계부 이름 변경</p>
          <TextInput
            title="가계부 이름"
            placeholder="최대 10자"
            value={title}
            onChange={setTitle}
            isError={!isValid}
            errorMessage={errorMessage}
          />
        </div>
      </ModalFormContent>
    </Modal>
  );
};

export default AccountBookNameModal;
