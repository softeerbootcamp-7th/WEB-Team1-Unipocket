import type { PopoverContentProps } from '@radix-ui/react-popover';

import { useKeyboardNavigation } from '@/hooks/useKeyboardNavigation';

import Chip from '@/components/common/Chip';
import { DataTableOptionList } from '@/components/data-table/DataTableOptionList';
import { PopoverContent } from '@/components/ui/popover';

import { useGetTravelsQuery } from '@/api/travels/query';
import { NONE_TRAVEL } from '@/constants/column';

interface TravelSelectorContentProps extends PopoverContentProps {
  initialTravelId: number | string | null;
  onTravelSelect: (travelId: number | string) => void;
  onInteractOutside?: () => void;
}

export const TravelSelectorContent = ({
  initialTravelId,
  onTravelSelect,
  onInteractOutside,
  align = 'start',
  sideOffset = 0,
  className,
  ...props
}: TravelSelectorContentProps) => {
  const { data: travels = [] } = useGetTravelsQuery();

  const options = [NONE_TRAVEL, ...travels.map((travel) => travel.travelId)];

  const currentTravel = initialTravelId;

  const getTravelName = (travelId: number | string) => {
    if (travelId === NONE_TRAVEL) return NONE_TRAVEL; // '-' 표시
    const travel = travels.find((t) => t.travelId === travelId);
    return travel?.travelPlaceName || '알 수 없는 여행';
  };

  const initialIndex = Math.max(
    0,
    currentTravel !== null ? options.indexOf(currentTravel) : 0,
  );

  const { activeIndex, setActiveIndex, handleKeyDown } = useKeyboardNavigation<
    number | string
  >({
    items: options,
    initialActiveIndex: initialIndex,
    onSelect: onTravelSelect,
  });

  return (
    <PopoverContent
      align={align}
      sideOffset={sideOffset}
      className={`rounded-modal-8 border-line-solid-normal shadow-semantic-subtle bg-background-normal flex w-75 flex-col p-0 outline-none ${className || ''}`}
      onInteractOutside={onInteractOutside}
      onKeyDown={handleKeyDown}
      {...props}
    >
      <DataTableOptionList
        items={options}
        activeIndex={activeIndex}
        setActiveIndex={setActiveIndex}
        isSelected={(item) => currentTravel !== null && currentTravel === item}
        onSelect={onTravelSelect}
        renderItem={(item) => <Chip label={getTravelName(item)} />}
      />
    </PopoverContent>
  );
};
