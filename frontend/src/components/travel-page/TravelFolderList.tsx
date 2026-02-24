import { useState } from 'react';
import { Link } from '@tanstack/react-router';

import FolderCard from '@/components/travel-page/FolderCard';
import TravelContextMenu from '@/components/travel-page/TravelContextMenu';

import { useGetTravelsQuery } from '@/api/travels/query';
import type { TravelBase } from '@/api/travels/type';

interface TravelFolderListProps {
  onOpenEditThumbnail: (travelId: number, imageUrl: string | null) => void;
  onOpenEditName: (
    travelId: number,
    defaultName: string,
    startDate: string,
    endDate: string,
    imageKey: string,
  ) => void;
  onOpenEditDate: (
    travelId: number,
    startDate: string,
    endDate: string,
    travelPlaceName: string,
    imageKey: string,
  ) => void;
  onOpenDelete: (travelId: number) => void;
}

const TravelFolderList = ({
  onOpenEditThumbnail,
  onOpenEditName,
  onOpenEditDate,
  onOpenDelete,
}: TravelFolderListProps) => {
  const { data: travels = [] } = useGetTravelsQuery();

  const [contextMenu, setContextMenu] = useState<{
    x: number;
    y: number;
    folder: TravelBase;
  } | null>(null);

  const handleContextMenu = (e: React.MouseEvent, folder: TravelBase) => {
    e.preventDefault();
    setContextMenu({ x: e.clientX, y: e.clientY, folder });
  };

  return (
    <div className="bg-background-normal rounded-modal-8 shadow-semantic-subtle flex min-h-0 flex-1 flex-wrap gap-9 overflow-y-auto p-16">
      {travels.map((folder) => (
        <Link
          to={`/travel/$travelId`}
          key={folder.travelId}
          params={{ travelId: folder.travelId.toString() }}
          className="h-fit"
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
              contextMenu.folder.startDate,
              contextMenu.folder.endDate,
              contextMenu.folder.imageKey,
            );
            setContextMenu(null);
          }}
          onEditDate={() => {
            onOpenEditDate(
              contextMenu.folder.travelId,
              contextMenu.folder.startDate,
              contextMenu.folder.endDate,
              contextMenu.folder.travelPlaceName,
              contextMenu.folder.imageKey,
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
