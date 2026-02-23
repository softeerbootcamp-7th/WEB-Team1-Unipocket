import { useState } from 'react';
import type { PopoverContentProps } from '@radix-ui/react-popover';

import { useKeyboardNavigation } from '@/hooks/useKeyboardNavigation';

import Chip from '@/components/common/Chip';
import { DataTableOptionList } from '@/components/data-table/DataTableOptionList';
import { PopoverContent } from '@/components/ui/popover';

import { useGetTravelsQuery } from '@/api/travels/query';

interface TravelSelectorContentProps extends PopoverContentProps {
  initialTravelId?: number;
  onTravelSelect: (travelId: number) => void;
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
  const options = travels.map((travel) => travel.travelId);

  const [selectedTravelId, setSelectedTravelId] = useState<number | null>(
    initialTravelId ?? null,
  );

  const getTravelName = (travelId: number) => {
    const travel = travels.find((t) => t.travelId === travelId);
    return travel?.travelPlaceName || '알 수 없는 여행';
  };

  const initialIndex = Math.max(
    0,
    initialTravelId ? options.indexOf(initialTravelId) : 0,
  );

  const { activeIndex, setActiveIndex, handleKeyDown } =
    useKeyboardNavigation<number>({
      items: options,
      initialActiveIndex: initialIndex,
      onSelect: (selectedId) => {
        setSelectedTravelId(selectedId);
        onTravelSelect(selectedId);
      },
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
        isSelected={(item) => selectedTravelId === item}
        onSelect={(selectedId) => {
          setSelectedTravelId(selectedId);
          onTravelSelect(selectedId);
        }}
        renderItem={(item) => <Chip label={getTravelName(item)} />}
      />
    </PopoverContent>
  );
};
