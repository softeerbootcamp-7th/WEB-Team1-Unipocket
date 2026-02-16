import { useCallback, useState } from 'react';

import Button from '@/components/common/Button';
import { Popover, PopoverTrigger } from '@/components/ui/popover';
import FileUploadModal from '@/components/upload/file-upload/FileUploadModal';
import ImageUploadModal from '@/components/upload/image-upload/ImageUploadModal';
import UploadPopover from '@/components/upload/UploadPopover';

export type ModalType = 'image' | 'file' | null;

const UploadMenu = () => {
  const [isPopoverOpen, setIsPopoverOpen] = useState(false);
  const [activeModal, setActiveModal] = useState<ModalType>(null);
  const handleOpenModal = (type: Exclude<ModalType, null>) => {
    setActiveModal(type);
    setIsPopoverOpen(false);
  };

  const handleCloseModal = useCallback(() => {
    setActiveModal(null);
  }, []);

  return (
    <>
      <Popover open={isPopoverOpen} onOpenChange={setIsPopoverOpen}>
        <PopoverTrigger asChild>
          <Button variant="solid" size="md">
            지출 내역 추가하기
          </Button>
        </PopoverTrigger>
        <UploadPopover onOpenUpload={handleOpenModal} />
      </Popover>
      <ImageUploadModal
        isOpen={activeModal === 'image'}
        onClose={handleCloseModal}
      />
      <FileUploadModal
        isOpen={activeModal === 'file'}
        onClose={handleCloseModal}
      />
    </>
  );
};

export default UploadMenu;
