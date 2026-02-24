import { useState } from 'react';

import Snackbar from '@/components/common/Snackbar';
import Modal from '@/components/modal/Modal';
import ImageResultModal from '@/components/upload/image-upload/ImageResultModal';
import ImageUploadContent from '@/components/upload/image-upload/ImageUploadContent';
import { useImageUpload } from '@/components/upload/image-upload/useImageUpload';

import { useRequiredAccountBook } from '@/stores/accountBookStore';

interface ImageUploadModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const ImageUploadModal = ({ isOpen, onClose }: ImageUploadModalProps) => {
  const accountBookId = useRequiredAccountBook().accountBookId;
  const [isResultModalOpen, setIsResultModalOpen] = useState(false);

  const {
    items,
    handleFilesSelected,
    removeItem,
    isAllUploaded,
    startParsing,
    parseSnackbar,
    closeParseSnackbar,
    clearItems,
    parsedMetaId,
  } = useImageUpload(accountBookId);

  const handleStartParsing = () => {
    void startParsing().then((isStarted) => {
      if (isStarted) {
        onClose();
        clearItems();
      }
    });
  };

  return (
    <>
      <Modal
        isOpen={isOpen}
        onClose={onClose}
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

      {parseSnackbar.isOpen && (
        <Snackbar
          status={parseSnackbar.status}
          description={parseSnackbar.description}
          onAction={() => {
            closeParseSnackbar();
            if (parseSnackbar.status === 'success') {
              setIsResultModalOpen(true);
            }
          }}
        />
      )}

      {parsedMetaId !== undefined && isResultModalOpen && (
        <ImageResultModal
          isOpen={isResultModalOpen}
          accountBookId={accountBookId}
          metaId={parsedMetaId}
          onClose={() => setIsResultModalOpen(false)}
        />
      )}
    </>
  );
};

export default ImageUploadModal;
