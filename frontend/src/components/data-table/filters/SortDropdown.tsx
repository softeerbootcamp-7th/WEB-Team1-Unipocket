import { DataTableSelectFilter } from '@/components/data-table/DataTableFilter';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover';

import { Icons } from '@/assets';

const SortDropdown = () => {
  return (
    <Popover>
      <PopoverTrigger asChild>
        <Icons.SwapVertical className="h-5 w-5 cursor-pointer text-gray-600" />
      </PopoverTrigger>
      <PopoverContent
        align="end"
        className="bg-background-normal rounded-modal-12 shadow-semantic-subtle flex flex-col gap-4 p-4"
      >
        <p className="text-label-normal caption2-bold">정렬</p>
        <div className="flex gap-3">
          <DataTableSelectFilter
            title={''}
            options={[]}
            selectedOptions={[]}
            setSelectedOptions={function (selected: string[]): void {
              throw new Error('Function not implemented.');
            }}
            onInputChange={function (term: string): void {
              throw new Error('Function not implemented.');
            }}
            renderOption={function (
              option: string,
              searchTerm: string,
            ): React.ReactNode {
              throw new Error('Function not implemented.');
            }}
          />
        </div>
      </PopoverContent>
    </Popover>
  );
};

export default SortDropdown;
