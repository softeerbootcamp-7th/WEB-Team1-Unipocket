import { CATEGORY_STYLE, type CategoryType } from '@/types/category';

interface CategoryProps {
  type: CategoryType;
}

const Category = ({ type }: CategoryProps) => {
  const { bg, text } = CATEGORY_STYLE[type];
  return (
    <div className={`inline-flex items-center rounded-md ${bg} px-1.5 py-0.75`}>
      <span className={`category-semibold ${text}`}>{type}</span>
    </div>
  );
};

export default Category;
