import clsx from 'clsx';

interface OptionItemProps {
  id: number;
  label: string;
  isSelected: boolean;
  onSelect: (id: number) => void;
  isMain?: boolean;
}

const OptionItem = ({
  id,
  label,
  isSelected,
  onSelect,
  isMain,
}: OptionItemProps) => {
  return (
    <li>
      <button
        type="button"
        onClick={() => onSelect(id)}
        className={clsx(
          'label2-medium flex w-full items-center rounded-md p-2 text-left transition-colors',
          isSelected
            ? 'bg-primary-normal/10 text-primary-normal' // @TODO: bg 대신 chip 추가
            : 'hover:bg-fill-alternative text-label-normal',
        )}
      >
        {label}
        {isMain && (
          <span className="label3-medium text-label-neutral ml-2">(기본)</span>
        )}
      </button>
    </li>
  );
};

export default OptionItem;
