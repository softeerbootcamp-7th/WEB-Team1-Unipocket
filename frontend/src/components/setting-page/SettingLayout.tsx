const SettingSection = ({ children }: { children: React.ReactNode }) => {
  return <div className="flex items-start py-2.5">{children}</div>;
};

interface SettingRowProps {
  label: string;
  value: string;
  onEdit: () => void;
}

const SettingRow = ({ label, value, onEdit }: SettingRowProps) => {
  return (
    <div className="flex items-center py-2.5">
      <span className="body1-normal-bold text-label-normal w-44 shrink-0">
        {label}
      </span>
      <div className="flex items-center gap-6.5">
        <span className="body1-normal-medium text-label-neutral">{value}</span>
        <button
          onClick={onEdit}
          className="body1-normal-medium text-label-assistive shrink-0"
        >
          수정
        </button>
      </div>
    </div>
  );
};

export { SettingRow, SettingSection };
