import { useState } from 'react';

import Modal from '@/components/modal/Modal';
import ImageResultModal from '@/components/upload/image-upload/ImageResultModal';

import { Icons } from '@/assets';
import { useRequiredAccountBook } from '@/stores/accountBookStore';

import FileUploadContent from './FileUploadContent';
import { useFileUpload } from './useFileUpload';

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
    isParsing,
    clearItem,
  } = useFileUpload(accountBookId);

  const handleClose = () => {
    onClose();
    clearItem();
  };

  const handleStartParsing = async () => {
    const parsedMetaId = await startParsing();
    if (parsedMetaId !== undefined) {
      setResultMetaId(parsedMetaId);
      onClose();
      setIsResultModalOpen(true);
      clearItem();
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
        confirmButton={{
          label: (
            <span className="flex items-center gap-1.5">
              <span>결과 확인</span>
              {isParsing && (
                <Icons.Loading className="size-4 animate-spin text-white" />
              )}
            </span>
          ),
        }}
      >
        <FileUploadContent
          item={item}
          onFilesSelected={handleFilesSelected}
          onRemove={removeItem}
          isReady={isReady}
        />
      </Modal>

      {resultMetaId !== undefined && isResultModalOpen && (
        <ImageResultModal
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
