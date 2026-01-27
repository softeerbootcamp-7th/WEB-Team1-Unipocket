import clsx from 'clsx';

interface OptionItemProps {
  label: string;
  isSelected: boolean;
  onSelect: (value: string) => void;
}

const OptionItem = ({ label, isSelected, onSelect }: OptionItemProps) => {
  return (
    <li>
      <button
        type="button"
        onClick={() => onSelect(label)}
        className={clsx(
          'label2-medium flex w-full items-center p-2 text-left transition-colors',
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
