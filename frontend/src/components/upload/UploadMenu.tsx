import { useCallback, useState } from 'react';
import { toast } from 'sonner';

import Button from '@/components/common/Button';
import SidePanel from '@/components/side-panel/SidePanel';
import { Popover, PopoverTrigger } from '@/components/ui/popover';
import FileUploadModal from '@/components/upload/file-upload/FileUploadModal';
import ImageUploadModal from '@/components/upload/image-upload/ImageUploadModal';
import UploadPopover from '@/components/upload/UploadPopover';

import { useParseSnackbarStore } from '@/stores/parseSnackbarStore';

export type UploadEntryType = 'image' | 'file' | 'manual' | null;

const MAX_PARSE_COUNT = 3;

const UploadMenu = () => {
  const [isPopoverOpen, setIsPopoverOpen] = useState(false);
  const [activeEntry, setActiveEntry] = useState<UploadEntryType>(null);
  const snackbars = useParseSnackbarStore((state) => state.snackbars);

  const handleOpenEntry = (type: Exclude<UploadEntryType, null>) => {
    if (type !== 'manual' && snackbars.length >= MAX_PARSE_COUNT) {
      toast.warning(
        <span>
          최대 {MAX_PARSE_COUNT}개까지 동시에 분석할 수 있어요.
          <br />
          업로드한 내역을 확인한 후 추가로 업로드해 주세요.
        </span>,
      );
      setIsPopoverOpen(false);
      return;
    }
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
      {activeEntry === 'manual' && (
        <SidePanel isOpen onClose={handleCloseEntry} />
      )}
    </>
  );
};

export default UploadMenu;
