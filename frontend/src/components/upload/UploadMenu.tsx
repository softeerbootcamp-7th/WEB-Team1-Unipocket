import { useCallback, useState } from 'react';

import Button from '@/components/common/Button';
import SidePanelUI from '@/components/side-panel/SidePanelUI';
import { Popover, PopoverTrigger } from '@/components/ui/popover';
import FileUploadModal from '@/components/upload/file-upload/FileUploadModal';
import ImageUploadModal from '@/components/upload/image-upload/ImageUploadModal';
import UploadPopover from '@/components/upload/UploadPopover';

export type UploadEntryType = 'image' | 'file' | 'manual' | null;

const UploadMenu = () => {
  const [isPopoverOpen, setIsPopoverOpen] = useState(false);
  const [activeEntry, setActiveEntry] = useState<UploadEntryType>(null);
  const handleOpenEntry = (type: Exclude<UploadEntryType, null>) => {
    setActiveEntry(type);
    setIsPopoverOpen(false);
  };

  const handleCloseEntry = useCallback(() => {
    setActiveEntry(null);
  }, []);

  return (
    <>
      <Popover open={isPopoverOpen} onOpenChange={setIsPopoverOpen}>
        <PopoverTrigger asChild>
          <Button variant="solid" size="md">
            지출 내역 추가하기
          </Button>
        </PopoverTrigger>
        <UploadPopover onOpenUpload={handleOpenEntry} />
      </Popover>
      <ImageUploadModal
        isOpen={activeEntry === 'image'}
        onClose={handleCloseEntry}
      />
      <FileUploadModal
        isOpen={activeEntry === 'file'}
        onClose={handleCloseEntry}
      />
      <SidePanelUI
        isOpen={activeEntry === 'manual'}
        onClose={handleCloseEntry}
      />
    </>
  );
};

export default UploadMenu;
