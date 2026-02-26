import { useState } from 'react';

import TextConfirmModal from '@/components/modal/TextModal/TextConfirmModal';
import ImageResultContent from '@/components/upload/image-upload/ImageResultContent';
import UploadResultModal from '@/components/upload/UploadResultModal';

import { useGetMetaFilesQuery } from '@/api/temporary-expenses/query';
import { useConfirmMetaMutation } from '@/api/temporary-expenses/query';
import { useDeleteOutOfPeriodExpensesMutation } from '@/api/temporary-expenses/query';
import { useRequiredAccountBook } from '@/stores/accountBookStore';

interface ImageResultModalProps {
  isOpen: boolean;
  accountBookId: number;
  metaId: number;
  onClose: () => void;
  onConfirm?: () => void;
}

const ImageResultModal = ({
  isOpen,
  accountBookId,
  metaId,
  onClose,
  onConfirm,
}: ImageResultModalProps) => {
  const { data } = useGetMetaFilesQuery(accountBookId, metaId);
  const confirmMetaMutation = useConfirmMetaMutation(accountBookId);
  const { startDate, endDate } = useRequiredAccountBook();
  const deleteOutOfPeriodMutation = useDeleteOutOfPeriodExpensesMutation(
    accountBookId,
    metaId,
    startDate,
    endDate,
  );

  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

  const allExpenses = (data?.files ?? []).flatMap((f) => f.expenses);
  const outOfPeriodCount = allExpenses.filter((e) => {
    if (!e.occurredAt) return false;
    const date = e.occurredAt.split('T')[0];
    return date < startDate || date > endDate;
  }).length;
  const hasInPeriodExpenses = allExpenses.some((e) => {
    if (!e.occurredAt) return true;
    const date = e.occurredAt.split('T')[0];
    return date >= startDate && date <= endDate;
  });

  const executeConfirm = () => {
    const files = data?.files ?? [];

    deleteOutOfPeriodMutation.mutate(files, {
      onSuccess: () => {
        if (!hasInPeriodExpenses) {
          onClose();
          return;
        }
        confirmMetaMutation.mutate(metaId, {
          onSuccess: () => {
            onConfirm?.();
            onClose();
          },
        });
      },
    });
  };

  const handleConfirm = () => {
    if (outOfPeriodCount > 0) {
      setShowDeleteConfirm(true);
      return;
    }
    executeConfirm();
  };

  const hasAnyIssue =
    data?.files.some((file) => file.incompleteCount > 0) ?? false;

  const imageCount = data?.files.length ?? 0;
  const expenseCount =
    data?.files.reduce((sum, file) => sum + file.expenses.length, 0) ?? 0;

  return (
    <>
      <UploadResultModal
        isOpen={isOpen}
        imageCount={imageCount}
        expenseCount={expenseCount}
        onClose={onClose}
        onConfirm={handleConfirm}
        isConfirmDisabled={hasAnyIssue}
      >
        <ImageResultContent
          accountBookId={accountBookId}
          metaId={metaId}
          files={data?.files ?? []}
        />
      </UploadResultModal>
      <TextConfirmModal
        isOpen={showDeleteConfirm}
        onClose={() => setShowDeleteConfirm(false)}
        onAction={() => {
          setShowDeleteConfirm(false);
          executeConfirm();
        }}
        title="기간 외 지출 내역 삭제"
        description={`${outOfPeriodCount}개의 기간 외 지출내역이 삭제됩니다.`}
        confirmButton={{ label: '삭제 후 추가하기', variant: 'caution' }}
        backdropClassName="z-modal"
      />
    </>
  );
};

export default ImageResultModal;
