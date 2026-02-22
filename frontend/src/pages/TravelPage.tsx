import { Link } from '@tanstack/react-router';

import FolderCard from '@/components/travel-page/FolderCard';
import { folderMap } from '@/components/travel-page/mock';
import TravelHeader from '@/components/travel-page/TravelHeader';

const TravelPage = () => {
  return (
    <div className="flex flex-1 flex-col gap-4 px-30 py-8">
      <TravelHeader />
      <div className="bg-background-normal rounded-modal-8 shadow-semantic-subtle flex min-h-0 flex-1 flex-wrap gap-9 overflow-y-auto p-16">
        {folderMap.map((folder) => (
          <Link
            to={`/travel/$travelId`}
            key={folder.travelId}
            params={{ travelId: folder.travelId.toString() }}
          >
            <FolderCard
              label={folder.label}
              dateRange={folder.dateRange}
              localCountryCode={folder.localCountryCode}
              localCountryAmount={folder.localCountryAmount}
              baseCountryCode={folder.baseCountryCode}
              baseCountryAmount={folder.baseCountryAmount}
              imageUrl={folder.imageUrl}
            />
          </Link>
        ))}
      </div>
    </div>
  );
};

export default TravelPage;
