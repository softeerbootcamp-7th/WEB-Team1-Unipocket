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

  const handleConfirm = () => {
    const files = data?.files ?? [];
    const hasInPeriodExpenses = files
      .flatMap((f) => f.expenses)
      .some((e) => {
        if (!e.occurredAt) return true;
        const date = e.occurredAt.split('T')[0];
        return date >= startDate && date <= endDate;
      });

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

  const hasAnyIssue =
    data?.files.some((file) => file.incompleteCount > 0) ?? false;

  const imageCount = data?.files.length ?? 0;
  const expenseCount =
    data?.files.reduce((sum, file) => sum + file.expenses.length, 0) ?? 0;

  return (
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
  );
};

export default ImageResultModal;
