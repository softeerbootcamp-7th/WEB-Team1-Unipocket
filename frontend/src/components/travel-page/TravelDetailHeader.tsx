import { Link, useParams } from '@tanstack/react-router';

import { useWidgetContext } from '@/components/chart/widget/WidgetContext';
import Button from '@/components/common/Button';
import Divider from '@/components/common/Divider';
import Icon from '@/components/common/Icon';
import ExpenseCard from '@/components/home-page/ExpenseCard';

import {
  useGetTravelAmountQuery,
  useGetTravelDetailQuery,
} from '@/api/travels/query';
import type { CountryCode } from '@/data/country/countryCode';
import { formatDateToDot, formatTripDuration } from '@/lib/utils';

const TravelDetailHeader = () => {
  const { isWidgetEditMode, toggleEditMode } = useWidgetContext();
  const { travelId } = useParams({ from: '/_app/travel/$travelId' });
  const { data: amountData } = useGetTravelAmountQuery(travelId);

  return (
    <div className="flex items-end gap-4">
      <TripSummary />
      <Divider style="vertical" className="h-15" />
      <ExpenseCard
        label="총 지출"
        baseCountryCode={(amountData?.baseCountryCode ?? 'KR') as CountryCode}
        baseCountryAmount={amountData?.totalBaseAmount ?? 0}
        localCountryCode={(amountData?.localCountryCode ?? 'KR') as CountryCode}
        localCountryAmount={amountData?.totalLocalAmount ?? 0}
      />
      <div className="flex-1" />
      <Button
        variant={isWidgetEditMode ? 'solid' : 'outlined'}
        size="md"
        onClick={toggleEditMode}
      >
        {isWidgetEditMode ? '위젯 편집 완료하기' : '위젯 편집하기'}
      </Button>
    </div>
  );
};

export default TravelDetailHeader;

const TripSummary = () => {
  const { travelId } = useParams({ from: '/_app/travel/$travelId' });
  const { data: travel } = useGetTravelDetailQuery(travelId);

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-2.5">
        <Link to="/travel">
          <Icon iconName="ChevronBack" className="text-label-normal size-6" />
        </Link>
        <span className="heading2-bold text-label-normal">
          {travel?.travelPlaceName ?? ''}
        </span>
      </div>
      <div className="flex gap-1.5">
        {travel && (
          <>
            <span className="body1-normal-medium text-label-normal">
              {formatDateToDot(travel.startDate)} -{' '}
              {formatDateToDot(travel.endDate)}
            </span>
            <span className="body1-normal-medium text-label-alternative">
              {formatTripDuration(travel.startDate, travel.endDate)}
            </span>
          </>
        )}
      </div>
    </div>
  );
};
