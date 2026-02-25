import { useGetTravelImageUrlQuery } from '@/api/travels/query';
import type { CountryCode } from '@/data/country/countryCode';

import Folder from './Folder';

interface FolderCardProps {
  travelPlaceName: string;
  startDate: string;
  endDate: string;
  localCountryCode?: CountryCode;
  localCountryAmount?: number;
  baseCountryCode?: CountryCode;
  baseCountryAmount?: number;
  imageKey: string | null;
  onContextMenu?: (e: React.MouseEvent) => void;
}

const FolderCard = ({
  travelPlaceName,
  startDate,
  endDate,
  localCountryCode,
  localCountryAmount,
  baseCountryCode,
  baseCountryAmount,
  imageKey,
  onContextMenu,
}: FolderCardProps) => {
  const { data: imageUrlData } = useGetTravelImageUrlQuery(imageKey);
  const viewUrl = imageUrlData?.presignedUrl ?? null;

  return (
    <div
      className="flex w-46.5 flex-col items-center gap-4.5"
      onContextMenu={onContextMenu}
    >
      <Folder
        imageKey={viewUrl}
        localCountryCode={localCountryCode}
        localCountryAmount={localCountryAmount}
        baseCountryCode={baseCountryCode}
        baseCountryAmount={baseCountryAmount}
      />
      <div className="flex flex-col gap-1.5 text-center">
        <h3 className="body1-normal-bold text-label-normal">
          {travelPlaceName}
        </h3>
        <p className="figure-body2-14-semibold text-label-alternative">
          {`${startDate} - ${endDate}`}
        </p>
      </div>
    </div>
  );
};

export default FolderCard;
