import { useState } from 'react';

import Button from '@/components/common/Button';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover';
import ImageUploadModal from '@/components/upload/image-upload/ImageUploadModal';

import { Icons } from '@/assets';

interface UploadMenuItemProps {
  Icon: (typeof Icons)[keyof typeof Icons];
  title: string;
  subTitle: string;
  onClick?: () => void;
}

const UploadMenuItem = ({
  Icon,
  title,
  subTitle,
  onClick,
}: UploadMenuItemProps) => {
  return (
    <button
      onClick={onClick}
      className="flex w-full cursor-pointer flex-row items-center gap-5 rounded-xl py-4 pr-5 pl-3 text-left transition-colors hover:bg-gray-50 active:bg-gray-100"
    >
      <div className="rounded-modal-10 bg-fill-normal p-2.75">
        <Icon className="h-6 w-6" />
      </div>
      <div className="flex w-55 flex-col items-start gap-1">
        <span className="body2-normal-bold text-label-normal">{title}</span>
        <span className="label1-normal-medium text-label-alternative break-keep">
          {subTitle}
        </span>
      </div>
    </button>
  );
};

interface UploadPopoverProps {
  onOpenImageUpload: () => void;
}

const UploadPopover = ({ onOpenImageUpload }: UploadPopoverProps) => {
  return (
    <PopoverContent
      align="end"
      sideOffset={12}
      className="rounded-modal-20 border-line-normal-alternative shadow-backdrop flex w-fit flex-col items-center justify-center gap-2 border bg-white px-5 py-3.5"
    >
      <UploadMenuItem
        Icon={Icons.Phone}
        title="모바일로 업로드"
        subTitle="휴대폰에 저장된 결제 자료를 바로 업로드해 지출 내역으로 등록하세요."
        onClick={() => console.log('모바일 업로드 클릭')}
      />

      <UploadMenuItem
        Icon={Icons.Camera}
        title="영수증 / 은행 앱 사진 업로드"
        subTitle="사진 속 결제 정보를 자동으로 인식해요."
        onClick={onOpenImageUpload}
      />

      <UploadMenuItem
        Icon={Icons.FileBox}
        title="거래 내역 파일 업로드"
        subTitle="은행·카드사에서 받은 내역 파일을 올려주세요."
        onClick={() => console.log('파일 업로드 클릭')}
      />

      <UploadMenuItem
        Icon={Icons.Edit}
        title="직접 입력"
        subTitle="현지 결제, 현금 사용 내역을 기록해요."
        onClick={() => console.log('직접 입력 클릭')}
      />
    </PopoverContent>
  );
};

const UploadMenu = () => {
  const [isPopoverOpen, setIsPopoverOpen] = useState(false);
  const [isImageUploadOpen, setIsImageUploadOpen] = useState(false);

  const handleOpenImageUpload = () => {
    setIsImageUploadOpen(true);
    setIsPopoverOpen(false);
  };

  return (
    <>
      <Popover open={isPopoverOpen} onOpenChange={setIsPopoverOpen}>
        <PopoverTrigger asChild>
          <Button variant="solid" size="md">
            지출 내역 추가하기
          </Button>
        </PopoverTrigger>
        <UploadPopover onOpenImageUpload={handleOpenImageUpload} />
      </Popover>
      <ImageUploadModal
        isOpen={isImageUploadOpen}
        onClose={() => setIsImageUploadOpen(false)}
      />
    </>
  );
};

export default UploadMenu;
