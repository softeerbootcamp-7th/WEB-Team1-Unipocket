import Modal from '@/components/modal/Modal';
import ImageUploadContent from '@/components/upload/image-upload/ImageUploadContent';
import { useImageUpload } from '@/components/upload/image-upload/useImageUpload';

import { useRequiredAccountBook } from '@/stores/accountBookStore';

interface ImageUploadModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const ImageUploadModal = ({ isOpen, onClose }: ImageUploadModalProps) => {
  const accountBookId = useRequiredAccountBook().accountBookId;
  const {
    items,
    handleFilesSelected,
    removeItem,
    isAllUploaded,
    startParsing,
    clearItems,
    resetAll,
  } = useImageUpload(accountBookId);

  const handleStartParsing = () => {
    void startParsing().then((isStarted) => {
      if (isStarted) {
        onClose();
        clearItems();
      }
    });
  };

  const handleClose = () => {
    onClose();
    resetAll();
  };

  return (
    <>
      <Modal
        isOpen={isOpen}
        onClose={handleClose}
        onAction={handleStartParsing}
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
    </>
  );
};

export default ImageUploadModal;
