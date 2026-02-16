import { useCallback, useState } from 'react';

import Button from '@/components/common/Button';
import { Popover, PopoverTrigger } from '@/components/ui/popover';
import FileUploadModal from '@/components/upload/file-upload/FileUploadModal';
import ImageUploadModal from '@/components/upload/image-upload/ImageUploadModal';
import UploadPopover from '@/components/upload/UploadPopover';

const UploadMenu = () => {
  const [isPopoverOpen, setIsPopoverOpen] = useState(false);
  const [activeModal, setActiveModal] = useState<'image' | 'file' | null>(null);
  const handleOpenImageUpload = () => {
    setActiveModal('image');
    setIsPopoverOpen(false);
  };

  const handleOpenFileUpload = () => {
    setActiveModal('file');
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
        <UploadPopover
          onOpenImageUpload={handleOpenImageUpload}
          onOpenFileUpload={handleOpenFileUpload}
        />
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
