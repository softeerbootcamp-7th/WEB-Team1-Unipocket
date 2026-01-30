interface ControlProps {
  value: string;
  name: string;
  checked: boolean;
  onChange: (value: string) => void;
}

const Control = ({ value, name, checked, onChange }: ControlProps) => {
  return (
    <label className="flex cursor-pointer items-center py-0.5">
      <input
        type="radio"
        name={name}
        value={value}
        checked={checked}
        onChange={() => onChange(value)}
        className="border-line-solid-normal bg-background-normal checked:border-primary-strong checked:bg-background-normal checked:ring-background-normal h-5 w-5 cursor-pointer appearance-none rounded-full border checked:border-[6px] checked:ring-1"
      />
    </label>
  );
};

export default Control;
