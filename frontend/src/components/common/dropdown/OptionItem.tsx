import clsx from 'clsx';

interface OptionItemProps {
  id: number;
  label: string;
  isSelected: boolean;
  onSelect: (id: number) => void;
}

const OptionItem = ({ id, label, isSelected, onSelect }: OptionItemProps) => {
  return (
    <li>
      <button
        type="button"
        onClick={() => onSelect(id)}
        className={clsx(
          'label2-medium flex w-full items-center p-2 text-left transition-colors rounded-md',
          isSelected
            ? 'bg-primary-normal/10 text-primary-normal' // @TODO: bg 대신 chip 추가
            : 'hover:bg-fill-alternative text-label-normal',
        )}
      >
        {label}
      </button>
    </li>
  );
};

export default OptionItem;
