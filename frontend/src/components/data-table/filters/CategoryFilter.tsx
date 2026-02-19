import { useState } from 'react';

import Chip from '@/components/common/Chip';
import { DataTableSearchFilter } from '@/components/data-table/DataTableFilter';

import { CATEGORIES, type CategoryId } from '@/types/category';

const CategoryFilter = () => {
  const categoryOptions = Object.keys(CATEGORIES).map(Number) as CategoryId[];

  const [selectedCategories, setSelectedCategories] = useState<CategoryId[]>(
    [],
  );

  return (
    <DataTableSearchFilter<CategoryId>
      title="카테고리"
      options={categoryOptions}
      selectedOptions={selectedCategories}
      setSelectedOptions={setSelectedCategories}
      isCategory
      renderOption={(categoryId) => <Chip id={categoryId} />}
      onInputChange={() => {
        // 타이핑 할 때마다
        // api 호출 작업 예정
      }}
    />
  );
};

export default CategoryFilter;
