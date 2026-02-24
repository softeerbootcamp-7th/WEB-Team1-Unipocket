import { useState } from 'react';

import Snackbar from '@/components/common/Snackbar';
import Modal from '@/components/modal/Modal';
import FileResultModal from '@/components/upload/file-upload/FileResultModal';
import FileUploadContent from '@/components/upload/file-upload/FileUploadContent';
import { useFileUpload } from '@/components/upload/file-upload/useFileUpload';

import { useRequiredAccountBook } from '@/stores/accountBookStore';

interface FileUploadModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const FileUploadModal = ({ isOpen, onClose }: FileUploadModalProps) => {
  const accountBookId = useRequiredAccountBook().accountBookId;
  const [isResultModalOpen, setIsResultModalOpen] = useState(false);
  const [resultMetaId, setResultMetaId] = useState<number | undefined>(
    undefined,
  );

  const {
    item,
    handleFilesSelected,
    removeItem,
    isReady,
    startParsing,
    parseSnackbar,
    closeParseSnackbar,
    parsedMetaId,
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

      {parseSnackbar.isOpen && (
        <Snackbar
          status={parseSnackbar.status}
          description={parseSnackbar.description}
          onAction={() => {
            closeParseSnackbar();
            if (
              parseSnackbar.status === 'success' &&
              parsedMetaId !== undefined
            ) {
              setResultMetaId(parsedMetaId);
              setIsResultModalOpen(true);
            }
          }}
        />
      )}

      {resultMetaId !== undefined && isResultModalOpen && (
        <FileResultModal
          isOpen={isResultModalOpen}
          accountBookId={accountBookId}
          metaId={resultMetaId}
          onClose={() => {
            setIsResultModalOpen(false);
            setResultMetaId(undefined);
          }}
        />
      )}
    </>
  );
};

export default FileUploadModal;
