import { useState } from 'react';

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
}

const DropDown = ({ selected, onSelect, options }: DropDownProps) => {
  const [isOpen, setIsOpen] = useState(false);

  const selectedName = options?.find((opt) => opt.id === selected)?.name;

  const handleOptionClick = (id: number) => {
    onSelect(id);
    setIsOpen(false);
  };

  return (
    <div className="relative inline-block w-fit">
      <Filter isOpen={isOpen} onClick={() => setIsOpen((v) => !v)}>
        {/* @TODO: 추후 기본값 처리 방법 변경 (API 연동) */}
        {selectedName || options?.[0].name} 
      </Filter>

      {isOpen && (
        <ul className="rounded-modal-10 border-line-solid-normal bg-background-normal absolute top-full left-0 z-50 mt-1.5 w-50 overflow-hidden border p-1 shadow-lg">
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
