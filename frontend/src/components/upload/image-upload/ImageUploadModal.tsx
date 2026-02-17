import { useState } from 'react';

import Modal from '@/components/modal/Modal';
import type { UploadImageItem } from '@/components/upload/image-upload/UploadImage';

import ImageUploadContent from './ImageUploadContent';

interface ImageUploadModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const ImageUploadModal = ({ isOpen, onClose }: ImageUploadModalProps) => {
  const [items, setItems] = useState<UploadImageItem[]>([]);

  const handleClose = () => {
    setItems([]);
    onClose();
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      onAction={() => {}}
      className="px-8 pb-4"
      confirmButton={{ label: '결과 확인' }}
    >
      <ImageUploadContent items={items} setItems={setItems} />
    </Modal>
  );
};

export default ImageUploadModal;
