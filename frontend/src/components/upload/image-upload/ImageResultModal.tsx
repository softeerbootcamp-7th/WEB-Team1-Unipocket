import ImageResultContent from '@/components/upload/image-upload/ImageResultContent';
import UploadResultModal from '@/components/upload/UploadResultModal';

interface ImageResultModalProps {
  isOpen: boolean;
  imageCount: number;
  expenseCount: number;
  onClose: () => void;
  onConfirm: () => void;
}

const ImageResultModal = ({
  isOpen,
  imageCount,
  expenseCount,
  onClose,
  onConfirm,
}: ImageResultModalProps) => {
  return (
    <UploadResultModal
      isOpen={isOpen}
      imageCount={imageCount}
      expenseCount={expenseCount}
      onClose={onClose}
      onConfirm={onConfirm}
    >
      <ImageResultContent />
    </UploadResultModal>
  );
};

export default ImageResultModal;
