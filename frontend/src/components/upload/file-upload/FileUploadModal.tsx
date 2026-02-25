import Modal from '@/components/modal/Modal';
import FileUploadContent from '@/components/upload/file-upload/FileUploadContent';
import { useFileUpload } from '@/components/upload/file-upload/useFileUpload';

import { useRequiredAccountBook } from '@/stores/accountBookStore';

interface FileUploadModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const FileUploadModal = ({ isOpen, onClose }: FileUploadModalProps) => {
  const accountBookId = useRequiredAccountBook().accountBookId;
  const {
    item,
    handleFilesSelected,
    removeItem,
    isReady,
    startParsing,
    clearItemAfterParseStart,
    clearItem,
  } = useFileUpload(accountBookId);

  const handleClose = () => {
    onClose();
    clearItem();
  };

  const handleStartParsing = async () => {
    const isStarted = await startParsing();
    if (isStarted) {
      onClose();
      clearItemAfterParseStart();
    }
  };

  return (
    <>
      <Modal
        isOpen={isOpen}
        onClose={handleClose}
        onAction={() => {
          void handleStartParsing();
        }}
        className="px-8 pb-4"
        confirmButton={{ label: '결과 확인' }}
      >
        <FileUploadContent
          item={item}
          onFilesSelected={handleFilesSelected}
          onRemove={removeItem}
          isReady={isReady}
        />
      </Modal>
    </>
  );
};

export default FileUploadModal;
