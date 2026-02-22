interface TravelContextMenuProps {
  x: number;
  y: number;
  onEditThumbnail: () => void;
  onEditName: () => void;
  onEditDate: () => void;
  onDelete: () => void;
  onClose: () => void;
}

const TravelContextMenu = ({
  x,
  y,
  onEditThumbnail,
  onEditName,
  onEditDate,
  onDelete,
  onClose,
}: TravelContextMenuProps) => {
  const menuItems = [
    { label: '썸네일 변경', action: onEditThumbnail },
    { label: '폴더명 수정', action: onEditName },
    { label: '기간 수정', action: onEditDate },
    { label: '삭제', action: onDelete },
  ];

  return (
    <div
      className="bg-background-normal shadow-semantic-subtle z-overlay rounded-modal-16 border-line-normal-neutral absolute flex flex-col items-start border p-2"
      style={{ top: y, left: x }}
    >
      {menuItems.map((item) => (
        <TravelContextMenuItem
          key={item.label}
          label={item.label}
          onAction={item.action}
          onClose={onClose}
        />
      ))}
    </div>
  );
};

export default TravelContextMenu;

interface TravelContextMenuItemProps {
  label: string;
  onAction: () => void;
  onClose: () => void;
}

const TravelContextMenuItem = ({
  label,
  onAction,
  onClose,
}: TravelContextMenuItemProps) => {
  return (
    <button
      className="body1-normal-bold text-label-normal hover:bg-fill-alternative rounded-modal-8 w-25 cursor-pointer overflow-hidden px-2 py-3 text-start text-ellipsis"
      onClick={(e) => {
        e.stopPropagation();
        onAction();
        onClose();
      }}
    >
      {label}
    </button>
  );
};
