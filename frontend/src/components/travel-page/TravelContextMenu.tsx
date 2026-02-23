import {
  Popover,
  PopoverAnchor,
  PopoverContent,
} from '@/components/ui/popover';

interface TravelContextMenuProps {
  x: number;
  y: number;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onEditThumbnail: () => void;
  onEditName: () => void;
  onEditDate: () => void;
  onDelete: () => void;
}

const TravelContextMenu = ({
  x,
  y,
  open,
  onOpenChange,
  onEditThumbnail,
  onEditName,
  onEditDate,
  onDelete,
}: TravelContextMenuProps) => {
  const menuItems = [
    { label: '썸네일 변경', action: onEditThumbnail },
    { label: '폴더명 수정', action: onEditName },
    { label: '기간 수정', action: onEditDate },
    { label: '삭제', action: onDelete },
  ];

  return (
    <Popover open={open} onOpenChange={onOpenChange}>
      <PopoverAnchor asChild>
        <div
          className="fixed size-1"
          style={{
            top: y,
            left: x,
          }}
        />
      </PopoverAnchor>

      <PopoverContent
        side="right"
        align="start"
        sideOffset={0}
        className="rounded-modal-16 border-line-normal-neutral bg-background-normal shadow-semantic-subtle flex flex-col items-start border p-2"
        role="menu"
      >
        {menuItems.map((item) => (
          <button
            key={item.label}
            role="menuitem"
            className="body1-normal-bold text-label-normal hover:bg-fill-alternative rounded-modal-8 w-25 cursor-pointer px-2 py-3 text-start"
            onClick={() => {
              item.action();
              onOpenChange(false);
            }}
          >
            {item.label}
          </button>
        ))}
      </PopoverContent>
    </Popover>
  );
};

export default TravelContextMenu;
