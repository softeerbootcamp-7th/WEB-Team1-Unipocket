import { useState } from 'react';

import TextConfirmModal from '@/components/modal/TextModal/TextConfirmModal';
import UploadResultModal from '@/components/upload/UploadResultModal';

import { useGetMetaFilesQuery } from '@/api/temporary-expenses/query';
import { useConfirmMetaMutation } from '@/api/temporary-expenses/query';
import { useDeleteOutOfPeriodExpensesMutation } from '@/api/temporary-expenses/query';
import { useRequiredAccountBook } from '@/stores/accountBookStore';

import FileResultContent from './FileResultContent';

interface FileResultModalProps {
  isOpen: boolean;
  accountBookId: number;
  metaId: number;
  onClose: () => void;
  onConfirm?: () => void;
}

const FileResultModal = ({
  isOpen,
  accountBookId,
  metaId,
  onClose,
  onConfirm,
}: FileResultModalProps) => {
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

  // 첫 번째 파일만 표시 (단건 파일 업로드)
  const file = data?.files?.[0] ?? null;
  const expenseCount = file?.expenses?.length ?? 0;

  return (
    <>
      <UploadResultModal
        isOpen={isOpen}
        expenseCount={expenseCount}
        onClose={onClose}
        onConfirm={handleConfirm}
      >
        <FileResultContent file={file} />
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

export default FileResultModal;
