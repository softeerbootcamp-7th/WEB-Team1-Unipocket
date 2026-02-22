import { Link } from '@tanstack/react-router';

import Button from '@/components/common/Button';
import FolderCard from '@/components/travel-page/FolderCard';
import { folderMap } from '@/components/travel-page/mock';

const TravelPage = () => {
  return (
    <div className="flex flex-1 flex-col px-30 py-8">
      <h1 className="title2-semibold text-label-normal mb-3">여행 포켓</h1>
      <h2 className="headline1-medium text-label-alternative mb-11">
        여행별로 지출을 정리해두는 공간이에요
      </h2>
      <div className="mb-4 flex gap-4">
        <Button variant="solid" size="md">
          여행 포켓 생성하기
        </Button>
        <Button variant="outlined" size="md">
          편집
        </Button>
      </div>
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
