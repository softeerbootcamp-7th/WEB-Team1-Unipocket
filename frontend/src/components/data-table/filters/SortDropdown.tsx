import { useState } from 'react';

import DropDown from '@/components/common/dropdown/Dropdown';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover';

import { Icons } from '@/assets';

const DROPDOWN_WIDTH_CLASS = 'w-24';

const CRITERIA = {
  DATE: 'date',
  AMOUNT: 'amount',
} as const;

const ORDER = {
  DESC: 'desc',
  ASC: 'asc',
} as const;

const LABELS = {
  [CRITERIA.DATE]: '날짜',
  [CRITERIA.AMOUNT]: '금액',
  [`${CRITERIA.DATE}_${ORDER.DESC}`]: '최신순',
  [`${CRITERIA.DATE}_${ORDER.ASC}`]: '오래된순',
  [`${CRITERIA.AMOUNT}_${ORDER.DESC}`]: '높은순',
  [`${CRITERIA.AMOUNT}_${ORDER.ASC}`]: '낮은순',
};

const CRITERIA_LIST = Object.values(CRITERIA);
const ORDER_LIST = Object.values(ORDER);

type SortCriteriaType = (typeof CRITERIA)[keyof typeof CRITERIA];
type SortOrderType = (typeof ORDER)[keyof typeof ORDER];

const SortDropdown = () => {
  const [sort, setSort] = useState<{
    criteria: SortCriteriaType;
    order: SortOrderType;
  }>({ criteria: CRITERIA.DATE, order: ORDER.DESC });

  const criteriaOptions = CRITERIA_LIST.map((key, index) => ({
    id: index,
    name: LABELS[key],
  }));

  const orderOptions = ORDER_LIST.map((key, index) => ({
    id: index,
    name: LABELS[`${sort.criteria}_${key}` as keyof typeof LABELS],
  }));

  const selectedCriteriaId = CRITERIA_LIST.indexOf(sort.criteria);
  const selectedOrderId = ORDER_LIST.indexOf(sort.order);

  const handleCriteriaSelect = (id: number) => {
    setSort((prev) => ({ ...prev, criteria: CRITERIA_LIST[id] }));
  };

  const handleOrderSelect = (id: number) => {
    setSort((prev) => ({ ...prev, order: ORDER_LIST[id] }));
  };

  return (
    <Popover>
      <PopoverTrigger asChild>
        <button className="p-1">
          <Icons.SwapVertical className="size-5 cursor-pointer" />
        </button>
      </PopoverTrigger>
      <PopoverContent
        align="end"
        sideOffset={6}
        className="bg-background-normal rounded-modal-12 shadow-semantic-subtle flex w-fit flex-col gap-4 p-4"
      >
        <p className="text-label-normal caption2-bold">정렬</p>
        <div className="flex gap-2">
          <div className={DROPDOWN_WIDTH_CLASS}>
            <DropDown
              size="md"
              align="left"
              itemWidth={DROPDOWN_WIDTH_CLASS}
              options={criteriaOptions}
              selected={selectedCriteriaId}
              onSelect={handleCriteriaSelect}
            />
          </div>
          <div className={DROPDOWN_WIDTH_CLASS}>
            <DropDown
              size="md"
              align="left"
              itemWidth={DROPDOWN_WIDTH_CLASS}
              options={orderOptions}
              selected={selectedOrderId}
              onSelect={handleOrderSelect}
            />
          </div>
        </div>
      </PopoverContent>
    </Popover>
  );
};

export default SortDropdown;
