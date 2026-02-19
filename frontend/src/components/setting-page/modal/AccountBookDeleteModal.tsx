import Modal from '@/components/modal/Modal';
import ModalFormContent from '@/components/setting-page/modal/ModalFormContent';

const AccountBookDeleteModal = ({
  accountBookTitle,
  isMain,
  isSubmitting,
  onClose,
  onConfirm,
}: {
  accountBookTitle: string;
  isMain: boolean;
  isSubmitting: boolean;
  onClose: () => void;
  onConfirm: () => void;
}) => {
  return (
    <Modal
      isOpen
      onClose={onClose}
      onAction={onConfirm}
      confirmButton={{ label: '삭제', variant: 'danger' }}
    >
      <ModalFormContent isActionReady={!isSubmitting}>
        <div className="flex w-80 flex-col gap-2 px-2">
          <p className="heading2-bold text-label-normal">가계부 삭제</p>
          <p className="body2-normal-regular text-label-assistive">
            {accountBookTitle} 가계부를 정말 삭제하시겠어요?
          </p>
          {isMain && (
            <p className="caption1-regular text-label-assistive">
              메인 가계부를 삭제하면 다음 가계부가 메인으로 설정돼요.
            </p>
          )}
        </div>
      </ModalFormContent>
    </Modal>
  );
};

export default AccountBookDeleteModal;
