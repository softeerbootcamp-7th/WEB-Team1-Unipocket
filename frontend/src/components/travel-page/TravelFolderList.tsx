import { useState } from 'react';
import { Link } from '@tanstack/react-router';

import FolderCard from '@/components/travel-page/FolderCard';
import { folderMap, type TravelPocket } from '@/components/travel-page/mock';
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

  const closeContextMenu = () => {
    setContextMenu(null);
  };

  return (
    <div
      className="bg-background-normal rounded-modal-8 shadow-semantic-subtle flex min-h-0 flex-1 flex-wrap gap-9 overflow-y-auto p-16"
      onClick={closeContextMenu}
    >
      {folderMap.map((folder) => (
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
          onEditThumbnail={() => {}}
          onEditName={() => {}}
          onEditDate={() => {}}
          onDelete={() => {}}
          onClose={closeContextMenu}
        />
      )}
    </div>
  );
};

export default TravelFolderList;
