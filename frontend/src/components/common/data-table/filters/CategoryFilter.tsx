import { useState } from 'react';

import { CATEGORY_STYLE, type CategoryType } from '@/types/category';

import Tag from '../../Chip';
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
      renderOption={(category) => <Tag type={category} />}
      onInputChange={() => {
        // 타이핑 할 때마다
        // api 호출 작업 예정
      }}
    />
  );
};

export default CategoryFilter;
