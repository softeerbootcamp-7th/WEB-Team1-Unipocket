import type React from 'react';

export interface ValueItemProps {
  label: string;
  value: React.ReactNode;
  onClick?: () => void; // @TODO: 필수로 변경 예정
}

interface ValueContainerProps {
  items: ValueItemProps[];
}

const ValueItem = ({ label, value, onClick }: ValueItemProps) => {
  return (
    <div className="flex h-8 items-center">
      <p className="label1-normal-bold text-label-alternative w-25">{label}</p>
      <div
        className="label1-normal-medium text-label-normal w-63 cursor-pointer px-1 hover:opacity-70"
        onClick={onClick}
      >
        {value}
      </div>
    </div>
  );
};

const ValueContainer = ({ items }: ValueContainerProps) => {
  return (
    <div className="relative flex flex-col gap-2" data-value-container>
      {items.map(({ label, value, onClick }) => (
        <ValueItem key={label} label={label} value={value} onClick={onClick} />
      ))}
    </div>
  );
};

export default ValueContainer;
