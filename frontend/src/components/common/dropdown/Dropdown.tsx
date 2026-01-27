import { useState } from 'react';

import Filter from '../Filter';

import OptionItem from './OptionItem';

interface Option {
  id: string;
  name: string;
}

interface DropDownProps {
  selected: string | null;
  onSelect: (value: string) => void;
  options?: Option[];
}

const DEFAULT_PLACEHOLDER = '미국 교환학생';

const DropDown = ({ selected, onSelect, options }: DropDownProps) => {
  const [isOpen, setIsOpen] = useState(false);

  const handleOptionClick = (value: string) => {
    onSelect(value);
    setIsOpen(false);
  };

  return (
    <div className="relative inline-block w-fit">
      <Filter isOpen={isOpen} onClick={() => setIsOpen(v => !v)}>
        {selected || DEFAULT_PLACEHOLDER}
      </Filter>

      {isOpen && (
        <ul className="absolute left-0 top-full z-50 p-1 mt-1.5 w-50 overflow-hidden rounded-modal-10 border border-line-solid-normal bg-background-normal shadow-lg">
          {options?.map((option) => (
            <OptionItem
              key={option.id}
              label={option.name}
              isSelected={option.name === selected}
              onSelect={handleOptionClick}
            />
          ))}
        </ul>
      )}
    </div>
  );
};

export default DropDown;
