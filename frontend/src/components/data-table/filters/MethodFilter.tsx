import { useDataTableFilter } from '@/components/data-table/context';
import HighlightText from '@/components/data-table/filters/HighlightText';

import { useGetCardsQuery } from '@/api/users/query';

import { DataTableSearchFilter } from '../DataTableFilter';

const MethodFilter = () => {
  const { filter, updateFilter } = useDataTableFilter();

  const { data: cards } = useGetCardsQuery();

  const cardOptions = cards.map((card) => card.cardNumber);

  const selectedMethods = filter.cardNumber || [];

  const handleMethodChange = (selected: string[]) => {
    updateFilter({
      cardNumber: selected.length > 0 ? selected : undefined,
    });
  };

  const getNickName = (cardNumber: string) => {
    const card = cards.find((c) => c.cardNumber === cardNumber);
    return card?.nickName || cardNumber;
  };

  return (
    <DataTableSearchFilter<string>
      title="결제수단"
      options={cardOptions}
      selectedOptions={selectedMethods}
      setSelectedOptions={handleMethodChange}
      getDisplayLabel={(cardNumber) => getNickName(cardNumber)}
      filterFn={(cardNumber, term) => {
        const nickName = getNickName(cardNumber);
        return nickName.toLowerCase().includes(term.toLowerCase());
      }}
      renderOption={(cardNumber, searchTerm) => (
        <HighlightText text={getNickName(cardNumber)} highlight={searchTerm} />
      )}
    />
  );
};

export default MethodFilter;
