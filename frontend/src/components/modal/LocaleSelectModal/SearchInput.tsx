import { Icons } from '@/assets';

interface SearchInputProps {
  value: string;
  onChange: (value: string) => void;
}

const SearchInput = ({ value, onChange }: SearchInputProps) => {
  return (
    <div className="bg-fill-normal rounded-modal-10 flex h-14.25 w-full items-center gap-[4.8px] p-[14.4px]">
      <Icons.Search className="text-line-normal-normal size-5" />
      <input
        type="text"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder="검색어를 입력해주세요."
        className="placeholder:text-label-assistive placeholder:headline1-medium w-full focus:outline-none"
      />
    </div>
  );
};

export default SearchInput;
