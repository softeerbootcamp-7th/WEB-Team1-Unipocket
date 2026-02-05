import type { CategoryType } from '@/types/category';
import type { CurrencyType } from '@/types/currency';

interface CategoryListItemProps {
  currency: CurrencyType;
  categoryName: CategoryType;
  percentage: number;
  amount: string;
  color: string;
}

const CategoryListItem = ({
  currency,
  categoryName,
  percentage,
  amount,
  color,
}: CategoryListItemProps) => {
  return (
    <div className="flex h-6 w-52 items-center gap-2.5">
      <div className="h-3.5 w-3.5" style={{ backgroundColor: color }} />
      <div className="flex items-center gap-2.5">
        <span className="label1-normal-medium text-label-normal">
          {categoryName}
        </span>
        <span className="figure-body2-14-semibold text-label-alternative">
          {percentage}%
        </span>
      </div>
      <div className="flex flex-1 items-center justify-end gap-1">
        {currency === 'LOCAL' && (
          <div className="bg-fill-normal rounded-modal-4 h-3 w-6" />
        )}
        <span className="figure-body2-14-semibold text-label-normal">
          {amount}
        </span>
      </div>
    </div>
  );
};

export default CategoryListItem;
