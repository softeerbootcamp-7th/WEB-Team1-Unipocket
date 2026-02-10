import { useState } from 'react';

import { CATEGORY_STYLE, type CategoryType } from '@/types/category';

import Tag from '../../Tag';
import { DataTableSearchFilter } from '../DataTableFilter';

const CategoryFilter = () => {
  const categoryOptions = Object.keys(CATEGORY_STYLE) as CategoryType[];
  const [selectedCategories, setSelectedCategories] = useState<CategoryType[]>(
    [],
  );

  return (
    <DataTableSearchFilter<CategoryType>
      title="카테고리"
      options={categoryOptions}
      selectedOptions={selectedCategories}
      setSelectedOptions={setSelectedCategories}
      renderTag={(category, onRemove) => {
        return <Tag type={category} onRemove={onRemove} />;
      }}
      renderOption={(category) => <Tag type={category} />}
    />
  );
};

export default CategoryFilter;
