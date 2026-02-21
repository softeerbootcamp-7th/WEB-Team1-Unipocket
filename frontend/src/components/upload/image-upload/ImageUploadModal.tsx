import Modal from '@/components/modal/Modal';
import ImageUploadContent from '@/components/upload/image-upload/ImageUploadContent';
import { useImageUpload } from '@/components/upload/image-upload/useImageUpload';

import { useRequiredAccountBook } from '@/stores/accountBookStore';

interface ImageUploadModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const ImageUploadModal = ({ isOpen, onClose }: ImageUploadModalProps) => {
  const accountBookId = useRequiredAccountBook().id;

  const {
    items,
    handleFilesSelected,
    removeItem,
    isAllUploaded,
    startParsing,
  } = useImageUpload(accountBookId);

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      onAction={startParsing}
      confirmButton={{ label: '결과 확인' }}
      className="px-8 pb-4"
    >
      <ImageUploadContent
        items={items}
        onFilesSelected={handleFilesSelected}
        onRemove={removeItem}
        isAllUploaded={isAllUploaded}
      />
    </Modal>
  );
};

export default ImageUploadModal;
