import Modal from '@/components/modal/Modal';

import ImageUploadContent from './ImageUploadContent';

interface ImageUploadModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const ImageUploadModal = ({ isOpen, onClose }: ImageUploadModalProps) => {
  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      onAction={() => {}}
      className="px-8 pb-4"
      confirmButton={{ label: '결과 확인' }}
    >
      <ImageUploadContent />
    </Modal>
  );
};

export default ImageUploadModal;
