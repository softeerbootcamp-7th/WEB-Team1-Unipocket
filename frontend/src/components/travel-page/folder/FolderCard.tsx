import { useGetTravelImageUrlQuery } from '@/api/travels/query';
import type { CountryCode } from '@/data/country/countryCode';

import Folder from './Folder';

interface FolderCardProps {
  travelPlaceName: string;
  startDate: string;
  endDate: string;
  localCountryCode?: string;
  localCurrencyAmount?: number;
  baseCountryCode?: string;
  baseCurrencyAmount?: number;
  imageKey: string | null;
  onContextMenu?: (e: React.MouseEvent) => void;
}

const FolderCard = ({
  travelPlaceName,
  startDate,
  endDate,
  localCountryCode,
  localCurrencyAmount,
  baseCountryCode,
  baseCurrencyAmount,
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
        localCountryCode={localCountryCode as CountryCode | undefined}
        localCountryAmount={localCurrencyAmount}
        baseCountryCode={baseCountryCode as CountryCode | undefined}
        baseCountryAmount={baseCurrencyAmount}
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
