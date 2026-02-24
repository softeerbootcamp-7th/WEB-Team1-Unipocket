import { PopoverContent } from '@/components/ui/popover';
import { type UploadEntryType } from '@/components/upload/UploadMenu';

import { Icons } from '@/assets';

interface UploadMenuItemProps {
  Icon: (typeof Icons)[keyof typeof Icons];
  title: string;
  subTitle: string;
  onClick?: () => void;
}

const UPLOAD_MENU_ITEMS = [
  {
    key: 'image',
    Icon: Icons.Camera,
    title: '영수증 / 은행 앱 사진 업로드',
    subTitle: '사진 속 결제 정보를 자동으로 인식해요.',
  },
  {
    key: 'file',
    Icon: Icons.FileBox,
    title: '거래 내역 파일 업로드',
    subTitle: '은행·카드사에서 받은 내역 파일을 올려주세요.',
  },
  {
    key: 'manual',
    Icon: Icons.Edit,
    title: '직접 입력',
    subTitle: '현지 결제, 현금 사용 내역을 기록해요.',
  },
] as const;

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
  onOpenUpload: (type: Exclude<UploadEntryType, null>) => void;
}

const UploadPopover = ({ onOpenUpload }: UploadPopoverProps) => {
  const getMenuItemClick = (key: (typeof UPLOAD_MENU_ITEMS)[number]['key']) => {
    switch (key) {
      case 'image':
        return () => onOpenUpload('image');
      case 'file':
        return () => onOpenUpload('file');
      case 'manual':
        return () => onOpenUpload('manual');
      default:
        return undefined;
    }
  };

  return (
    <PopoverContent
      align="end"
      sideOffset={12}
      className="rounded-modal-20 border-line-normal-alternative shadow-backdrop flex w-fit flex-col items-center justify-center gap-2 border bg-white px-5 py-3.5"
    >
      {UPLOAD_MENU_ITEMS.map((item) => (
        <UploadMenuItem
          key={item.key}
          Icon={item.Icon}
          title={item.title}
          subTitle={item.subTitle}
          onClick={getMenuItemClick(item.key)}
        />
      ))}
    </PopoverContent>
  );
};

export default UploadPopover;
