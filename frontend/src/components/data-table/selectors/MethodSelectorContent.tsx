import type { PopoverContentProps } from '@radix-ui/react-popover';

import { useKeyboardNavigation } from '@/hooks/useKeyboardNavigation';

import Chip from '@/components/common/Chip';
import { DataTableOptionList } from '@/components/data-table/DataTableOptionList';
import { PopoverContent } from '@/components/ui/popover';

import { useGetCardsQuery } from '@/api/users/query';
import { CASH } from '@/constants/column';

interface MethodSelectorContentProps extends PopoverContentProps {
  initialCardNumber: string | null;
  onMethodSelect: (cardNumber: string) => void;
  onInteractOutside?: () => void;
}

export const MethodSelectorContent = ({
  initialCardNumber,
  onMethodSelect,
  onInteractOutside,
  align = 'start',
  sideOffset = 0,
  className,
  ...props
}: MethodSelectorContentProps) => {
  const { data: cards = [] } = useGetCardsQuery();
  const options = [CASH, ...cards.map((card) => card.cardNumber)];

  const currentMethod = initialCardNumber;

  const getNickName = (cardNumber: string) => {
    if (cardNumber === CASH) return CASH;
    const card = cards.find((c) => c.cardNumber === cardNumber);
    return card?.nickName || cardNumber;
  };

  const initialIndex = Math.max(
    0,
    currentMethod !== null ? options.indexOf(currentMethod) : 0,
  );

  const { activeIndex, setActiveIndex, handleKeyDown } =
    useKeyboardNavigation<string>({
      items: options,
      initialActiveIndex: initialIndex,
      onSelect: onMethodSelect,
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
        isSelected={(item) => currentMethod === item}
        onSelect={onMethodSelect}
        renderItem={(item) => <Chip label={getNickName(item)} />}
      />
    </PopoverContent>
  );
};
