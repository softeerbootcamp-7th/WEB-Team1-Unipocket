import UploadResultModal from '@/components/upload/UploadResultModal';

import { useGetMetaFilesQuery } from '@/api/temporary-expenses/query';
import { useConfirmMetaMutation } from '@/api/temporary-expenses/query';

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

  const handleConfirm = () => {
    confirmMetaMutation.mutate(metaId, {
      onSuccess: () => {
        onConfirm?.();
        onClose();
      },
    });
  };

  // 첫 번째 파일만 표시 (단건 파일 업로드)
  const file = data?.files?.[0] ?? null;
  const expenseCount = file?.expenses?.length ?? 0;

  return (
    <UploadResultModal
      isOpen={isOpen}
      expenseCount={expenseCount}
      onClose={onClose}
      onConfirm={handleConfirm}
    >
      <FileResultContent file={file} />
    </UploadResultModal>
  );
};

export default FileResultModal;
