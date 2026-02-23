import DropDown from '@/components/common/dropdown/Dropdown';
import Icon from '@/components/common/Icon';
import { useDataTableFilter } from '@/components/data-table/context';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover';

const DROPDOWN_WIDTH_CLASS = 'w-32';

const CRITERIA = {
  DATE: 'occurredAt',
  AMOUNT: 'baseCurrencyAmount',
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
  const { filter, updateFilter } = useDataTableFilter();

  const currentSort = filter.sort?.[0] || `${CRITERIA.DATE},${ORDER.DESC}`;
  const [currentCriteria, currentOrder] = currentSort.split(',') as [
    SortCriteriaType,
    SortOrderType,
  ];

  const criteriaOptions = CRITERIA_LIST.map((key, index) => ({
    id: index,
    name: LABELS[key],
  }));

  const orderOptions = ORDER_LIST.map((key, index) => ({
    id: index,
    name: LABELS[`${currentCriteria}_${key}` as keyof typeof LABELS],
  }));

  const selectedCriteriaId = Math.max(
    0,
    CRITERIA_LIST.indexOf(currentCriteria),
  );
  const selectedOrderId = Math.max(0, ORDER_LIST.indexOf(currentOrder));

  const handleCriteriaSelect = (id: number) => {
    const newCriteria = CRITERIA_LIST[id];
    updateFilter({ sort: [`${newCriteria},${currentOrder}`] });
  };

  const handleOrderSelect = (id: number) => {
    const newOrder = ORDER_LIST[id];
    updateFilter({ sort: [`${currentCriteria},${newOrder}`] });
  };

  return (
    <Popover>
      <PopoverTrigger asChild>
        <button>
          <Icon
            color="text-label-neutral"
            iconName="SwapVertical"
            width={20}
            height={20}
          />
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
              selectedId={selectedCriteriaId}
              onSelect={handleCriteriaSelect}
            />
          </div>
          <div className={DROPDOWN_WIDTH_CLASS}>
            <DropDown
              size="md"
              align="left"
              itemWidth={DROPDOWN_WIDTH_CLASS}
              options={orderOptions}
              selectedId={selectedOrderId}
              onSelect={handleOrderSelect}
            />
          </div>
        </div>
      </PopoverContent>
    </Popover>
  );
};

export default SortDropdown;
