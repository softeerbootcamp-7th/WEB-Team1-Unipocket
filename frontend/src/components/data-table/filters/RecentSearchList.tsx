import { Checkbox } from '@/components/ui/checkbox';

import { Icons } from '@/assets';

interface RecentSearchListProps {
  title?: string;
  searches: string[];
  selectedItems: string[];
  onToggle: (item: string) => void;
  onRemove: (item: string) => void;
}

const RecentSearchList = ({
  title = '최근 검색어',
  searches,
  selectedItems,
  onToggle,
  onRemove,
}: RecentSearchListProps) => {
  if (searches.length === 0) return null;

  return (
    <div className="caption2-bold min-h-33">
      <span className="text-label-alternative">{title}</span>
      {searches.map((keyword) => (
        <div
          key={keyword}
          onClick={() => onToggle(keyword)}
          className="group mt-2.5 flex cursor-pointer items-center gap-2.5 p-1"
        >
          <Checkbox checked={selectedItems.includes(keyword)} />
          <span className="text-label-neutral caption1-medium pt-0.5">
            {keyword}
          </span>
          <button
            onClick={(e) => {
              e.stopPropagation();
              onRemove(keyword);
            }}
            className="ml-auto opacity-0 transition group-hover:opacity-100"
          >
            <Icons.Close className="size-3.5" />
          </button>
        </div>
      ))}
    </div>
  );
};

export default RecentSearchList;
