import { useState } from 'react';
import { Link } from '@tanstack/react-router';

import FolderCard from '@/components/travel-page/FolderCard';
import {
  mockTravelPockets,
  type TravelPocket,
} from '@/components/travel-page/mock';
import TravelContextMenu from '@/components/travel-page/TravelContextMenu';

interface TravelFolderListProps {
  onOpenEditThumbnail: (travelId: number, imageUrl: string | null) => void;
  onOpenEditName: (travelId: number, defaultName: string) => void;
  onOpenEditDate: (
    travelId: number,
    startDate: string,
    endDate: string,
  ) => void;
  onOpenDelete: (travelId: number) => void;
}

const TravelFolderList = ({
  onOpenEditThumbnail,
  onOpenEditName,
  onOpenEditDate,
  onOpenDelete,
}: TravelFolderListProps) => {
  const [contextMenu, setContextMenu] = useState<{
    x: number;
    y: number;
    folder: TravelPocket;
  } | null>(null);

  const handleContextMenu = (e: React.MouseEvent, folder: TravelPocket) => {
    e.preventDefault();
    setContextMenu({ x: e.clientX, y: e.clientY, folder });
  };

  return (
    <div className="bg-background-normal rounded-modal-8 shadow-semantic-subtle flex min-h-0 flex-1 flex-wrap gap-9 overflow-y-auto p-16">
      {mockTravelPockets.map((folder) => (
        <Link
          to={`/travel/$travelId`}
          key={folder.travelId}
          params={{ travelId: folder.travelId.toString() }}
        >
          <FolderCard
            {...folder}
            onContextMenu={(e) => handleContextMenu(e, folder)}
          />
        </Link>
      ))}

      {contextMenu && (
        <TravelContextMenu
          x={contextMenu.x}
          y={contextMenu.y}
          open={true}
          onOpenChange={(open) => {
            if (!open) setContextMenu(null);
          }}
          onEditThumbnail={() => {
            onOpenEditThumbnail(
              contextMenu.folder.travelId,
              contextMenu.folder.imageKey,
            );
            setContextMenu(null);
          }}
          onEditName={() => {
            onOpenEditName(
              contextMenu.folder.travelId,
              contextMenu.folder.travelPlaceName,
            );
            setContextMenu(null);
          }}
          onEditDate={() => {
            onOpenEditDate(
              contextMenu.folder.travelId,
              contextMenu.folder.startDate,
              contextMenu.folder.endDate,
            );
            setContextMenu(null);
          }}
          onDelete={() => {
            onOpenDelete(contextMenu.folder.travelId);
            setContextMenu(null);
          }}
        />
      )}
    </div>
  );
};

export default TravelFolderList;
