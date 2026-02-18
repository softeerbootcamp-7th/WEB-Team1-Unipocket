import Modal from '@/components/modal/Modal';

import FileUploadContent from './FileUploadContent';

interface FileUploadModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const FileUploadModal = ({ isOpen, onClose }: FileUploadModalProps) => {
  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      onAction={() => {}}
      className="px-8 pb-4"
      confirmButton={{ label: '결과 확인' }}
    >
      <FileUploadContent />
    </Modal>
  );
};

export default FileUploadModal;
