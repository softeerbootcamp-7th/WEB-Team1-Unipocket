import { useGetMetaFilesQuery } from '@/api/temporary-expenses/query';
import ImageResultContent from '@/components/upload/image-upload/ImageResultContent';
import UploadResultModal from '@/components/upload/UploadResultModal';

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

  const imageCount = data?.files.length ?? 0;
  const expenseCount =
    data?.files.reduce((sum, file) => sum + file.expenses.length, 0) ?? 0;

  return (
    <UploadResultModal
      isOpen={isOpen}
      imageCount={imageCount}
      expenseCount={expenseCount}
      onClose={onClose}
      onConfirm={onConfirm}
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
