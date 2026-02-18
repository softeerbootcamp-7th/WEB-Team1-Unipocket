import { useState } from 'react';

import Chip from '@/components/common/Chip';
import { DataTableSearchFilter } from '@/components/data-table/DataTableFilter';

import {
  CATEGORIES,
  type CategoryType,
  getCategoryName,
} from '@/types/category';

const CategoryFilter = () => {
  const categoryOptions = Object.keys(CATEGORIES).map(Number) as CategoryType[];

  const [selectedCategories, setSelectedCategories] = useState<CategoryType[]>(
    [],
  );

  return (
    <DataTableSearchFilter<CategoryType>
      title="카테고리"
      options={categoryOptions}
      selectedOptions={selectedCategories}
      setSelectedOptions={setSelectedCategories}
      getOptionLabel={(id) => getCategoryName(id)}
      renderOption={(categoryId) => (
        <Chip
          label={getCategoryName(categoryId)}
          bg={CATEGORIES[categoryId].bg}
          text={CATEGORIES[categoryId].text}
        />
      )}
      onInputChange={() => {
        // 타이핑 할 때마다
        // api 호출 작업 예정
      }}
    />
  );
};

export default CategoryFilter;
