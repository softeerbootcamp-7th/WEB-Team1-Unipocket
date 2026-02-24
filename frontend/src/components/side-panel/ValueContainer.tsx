import type React from 'react';

import { Popover, PopoverTrigger } from '@/components/ui/popover';

export interface ValueItemProps {
  label: string;
  value: React.ReactNode;
  onClick?: () => void;
  popoverContent?: React.ReactNode;
  isPopoverOpen?: boolean;
  onPopoverOpenChange?: (open: boolean) => void;
}

interface ValueContainerProps {
  items: ValueItemProps[];
}

const ValueItem = ({
  label,
  value,
  onClick,
  popoverContent,
  isPopoverOpen,
  onPopoverOpenChange,
}: ValueItemProps) => {
  const valueEl = (
    <div
      className="label1-normal-medium text-label-normal flex h-8 w-63 cursor-pointer items-center px-1 hover:opacity-70"
      onClick={onClick}
    >
      {value}
    </div>
  );

  return (
    <div className="flex h-8 items-center">
      <p className="label1-normal-bold text-label-alternative w-25">{label}</p>
      {popoverContent ? (
        <Popover
          open={isPopoverOpen}
          onOpenChange={onPopoverOpenChange}
          modal={false}
        >
          <PopoverTrigger asChild>{valueEl}</PopoverTrigger>
          {popoverContent}
        </Popover>
      ) : (
        valueEl
      )}
    </div>
  );
};

const ValueContainer = ({ items }: ValueContainerProps) => {
  return (
    <div className="flex flex-col gap-2" data-value-container>
      {items.map(
        ({
          label,
          value,
          onClick,
          popoverContent,
          isPopoverOpen,
          onPopoverOpenChange,
        }) => (
          <ValueItem
            key={label}
            label={label}
            value={value}
            onClick={onClick}
            popoverContent={popoverContent}
            isPopoverOpen={isPopoverOpen}
            onPopoverOpenChange={onPopoverOpenChange}
          />
        ),
      )}
    </div>
  );
};

export default ValueContainer;
