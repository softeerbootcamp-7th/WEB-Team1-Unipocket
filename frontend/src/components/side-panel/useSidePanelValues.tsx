import { useState } from 'react';

import { formatDateTime } from '@/components/calendar/date.utils';
import Chip, { CategoryChip } from '@/components/common/Chip';
import { CategorySelectorContent } from '@/components/data-table/selectors/CategorySelectorContent';
import { MethodSelectorContent } from '@/components/data-table/selectors/MethodSelectorContent';
import { TravelSelectorContent } from '@/components/data-table/selectors/TravelSelectorContent';
import EmptyValue from '@/components/side-panel/EmptyValue';
import type { ValueItemProps } from '@/components/side-panel/ValueContainer';

import type { CategoryId } from '@/types/category';

import { useGetTravelsQuery } from '@/api/travels/query';
import { useGetCardsQuery } from '@/api/users/query';
import { CASH, NONE_TRAVEL } from '@/constants/column';

interface UseSidePanelValuesParams {
  selectedDateTime: Date | null;
  onDateTimeClick: () => void;
  selectedCategory: CategoryId | null;
  onCategorySelect: (id: CategoryId) => void;
  selectedCardNumber: string | null;
  onCardNumberSelect: (cardNumber: string) => void;
  selectedTravelId: number | string | null;
  onTravelSelect: (travelId: number | string) => void;
}

export const useSidePanelValues = ({
  selectedDateTime,
  onDateTimeClick,
  selectedCategory,
  onCategorySelect,
  selectedCardNumber,
  onCardNumberSelect,
  selectedTravelId,
  onTravelSelect,
}: UseSidePanelValuesParams): ValueItemProps[] => {
  const [isCategoryOpen, setIsCategoryOpen] = useState(false);
  const [isMethodOpen, setIsMethodOpen] = useState(false);
  const [isTravelOpen, setIsTravelOpen] = useState(false);

  const { data: cards = [] } = useGetCardsQuery();
  const { data: travels = [] } = useGetTravelsQuery();

  const getCardDisplayValue = () => {
    if (selectedCardNumber === null) return <EmptyValue />;
    if (selectedCardNumber === CASH) return <Chip label="현금" />;
    const card = cards.find((c) => c.cardNumber === selectedCardNumber);
    return <Chip label={card?.nickName ?? selectedCardNumber} />;
  };

  const getTravelDisplayValue = () => {
    if (selectedTravelId === null) return <EmptyValue />;
    if (selectedTravelId === NONE_TRAVEL) return <Chip label="-" />;
    const travel = travels.find((t) => t.travelId === selectedTravelId);
    return <Chip label={travel?.travelPlaceName ?? '알 수 없는 여행'} />;
  };

  return [
    {
      label: '일시',
      value: selectedDateTime ? (
        formatDateTime(selectedDateTime)
      ) : (
        <EmptyValue />
      ),
      onClick: onDateTimeClick,
    },
    {
      label: '카테고리',
      value:
        selectedCategory !== null ? (
          <CategoryChip categoryId={selectedCategory} />
        ) : (
          <EmptyValue />
        ),
      isPopoverOpen: isCategoryOpen,
      onPopoverOpenChange: setIsCategoryOpen,
      popoverContent: (
        <CategorySelectorContent
          initialCategoryId={selectedCategory}
          onCategorySelect={(id) => {
            onCategorySelect(id);
            setIsCategoryOpen(false);
          }}
          onInteractOutside={() => setIsCategoryOpen(false)}
        />
      ),
    },
    {
      label: '결제 수단',
      value: getCardDisplayValue(),
      isPopoverOpen: isMethodOpen,
      onPopoverOpenChange: setIsMethodOpen,
      popoverContent: (
        <MethodSelectorContent
          initialCardNumber={selectedCardNumber}
          onMethodSelect={(cardNumber) => {
            onCardNumberSelect(cardNumber);
            setIsMethodOpen(false);
          }}
          onInteractOutside={() => setIsMethodOpen(false)}
        />
      ),
    },
    {
      label: '여행',
      value: getTravelDisplayValue(),
      isPopoverOpen: isTravelOpen,
      onPopoverOpenChange: setIsTravelOpen,
      popoverContent: (
        <TravelSelectorContent
          initialTravelId={selectedTravelId ?? NONE_TRAVEL}
          onTravelSelect={(travelId) => {
            onTravelSelect(travelId);
            setIsTravelOpen(false);
          }}
          onInteractOutside={() => setIsTravelOpen(false)}
        />
      ),
    },
  ];
};
