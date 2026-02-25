import { useDataTableFilter } from '@/components/data-table/context';
import HighlightText from '@/components/data-table/filters/HighlightText';

import { useGetCardsQuery } from '@/api/users/query';
import { CASH } from '@/constants/column'; // 💡 CASH 상수 import 추가

import { DataTableSearchFilter } from '../DataTableFilter';

const MethodFilter = () => {
  const { filter, updateFilter } = useDataTableFilter();

  const { data: cards = [] } = useGetCardsQuery();

  const cardOptions = [CASH, ...cards.map((card) => card.cardNumber)];

  // 💡 filter.isCash 상태와 filter.cardNumber 상태를 하나의 배열로 합쳐서 UI에 전달
  const selectedMethods = [
    ...(filter.isCash ? [CASH] : []),
    ...(filter.cardNumber || []),
  ];

  const handleMethodChange = (selected: string[]) => {
    // 💡 선택된 항목 중 CASH가 있는지 확인하고, 나머지는 카드 번호로 분리
    const isCashSelected = selected.includes(CASH);
    const cardNumbers = selected.filter((item) => item !== CASH);

    updateFilter({
      isCash: isCashSelected ? true : undefined,
      cardNumber: cardNumbers.length > 0 ? cardNumbers : undefined,
    });
  };

  const getNickName = (item: string) => {
    if (item === CASH) return CASH; // 💡 CASH일 경우 그대로 반환
    const card = cards.find((c) => c.cardNumber === item);
    return card?.nickName || item;
  };

  return (
    <DataTableSearchFilter<string>
      title="결제수단"
      options={cardOptions}
      selectedOptions={selectedMethods}
      setSelectedOptions={handleMethodChange}
      getDisplayLabel={(item) => getNickName(item)}
      filterFn={(item, term) => {
        const nickName = getNickName(item);
        return nickName.toLowerCase().includes(term.toLowerCase());
      }}
      renderOption={(item, searchTerm) => (
        <HighlightText text={getNickName(item)} highlight={searchTerm} />
      )}
    />
  );
};

export default MethodFilter;
