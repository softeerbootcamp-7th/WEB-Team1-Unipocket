import { useRef, useState } from 'react';
import { clsx } from 'clsx';

import { useClickOutside } from '@/hooks/useClickOutside';

import Filter from '../Filter';
import OptionItem from './OptionItem';

interface Option {
  id: number;
  name: string;
}

interface DropDownProps {
  selected: number | null;
  onSelect: (id: number) => void;
  options?: Option[];
  size?: 'xs' | 'sm' | 'md' | 'lg';
  align?: 'left' | 'right' | 'center';
  itemWidth?: string;
}

const ALIGN_CLASS = {
  left: 'left-0',
  right: 'right-0',
  center: 'left-1/2 -translate-x-1/2',
} as const;

const DropDown = ({
  selected,
  onSelect,
  options,
  size = 'sm',
  align = 'left',
  itemWidth = 'w-40',
}: DropDownProps) => {
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  useClickOutside(dropdownRef, () => {
    setIsOpen(false);
  });

  const selectedName = options?.find((opt) => opt.id === selected)?.name;

  const handleOptionClick = (id: number) => {
    onSelect(id);
    setIsOpen(false);
  };

  return (
    <div ref={dropdownRef} className="relative inline-block w-full">
      <Filter
        isOpen={isOpen}
        onClick={() => setIsOpen((v) => !v)}
        size={size}
        className="w-full"
      >
        {selectedName || options?.[0].name}
      </Filter>

      {isOpen && (
        <ul
          className={clsx(
            'scrollbar rounded-modal-10 border-line-solid-normal bg-background-normal absolute top-full z-50 mt-1.5 max-h-60 overflow-hidden overflow-y-auto border p-1 shadow-lg',
            ALIGN_CLASS[align],
            itemWidth,
          )}
        >
          {options?.map((option) => (
            <OptionItem
              key={option.id}
              id={option.id}
              label={option.name}
              isSelected={option.id === selected}
              onSelect={handleOptionClick}
            />
          ))}
        </ul>
      )}
    </div>
  );
};

export default DropDown;
