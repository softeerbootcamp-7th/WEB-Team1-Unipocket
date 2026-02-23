import { useState } from 'react';
import { Link } from '@tanstack/react-router';

import FolderCard from '@/components/travel-page/FolderCard';
import {
  mockTravelPockets,
  type TravelPocket,
} from '@/components/travel-page/mock';
import TravelContextMenu from '@/components/travel-page/TravelContextMenu';

const TravelFolderList = () => {
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
          onEditThumbnail={() => {}}
          onEditName={() => {}}
          onEditDate={() => {}}
          onDelete={() => {}}
        />
      )}
    </div>
  );
};

export default TravelFolderList;
