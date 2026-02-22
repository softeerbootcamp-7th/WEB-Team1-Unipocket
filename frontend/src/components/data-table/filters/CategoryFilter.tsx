import { CategoryChip } from '@/components/common/Chip';
import { useDataTableFilter } from '@/components/data-table/context';
import { DataTableSearchFilter } from '@/components/data-table/DataTableFilter';

import { CATEGORIES, type CategoryId } from '@/types/category';

const CategoryFilter = () => {
  const { filter, updateFilter } = useDataTableFilter();

  const categoryOptions = Object.keys(CATEGORIES).map(Number) as CategoryId[];

  // 전역 filter 객체에서 선택된 카테고리 배열 가져오기 (없으면 빈 배열)
  const selectedCategories = filter.category || [];

  const handleCategoryChange = (selected: CategoryId[]) => {
    updateFilter({
      category: selected.length > 0 ? selected : undefined,
    });
  };

  return (
    <DataTableSearchFilter<CategoryId>
      title="카테고리"
      options={categoryOptions}
      selectedOptions={selectedCategories}
      setSelectedOptions={handleCategoryChange}
      isCategory
      filterFn={(categoryId, term) =>
        CATEGORIES[categoryId].name.toLowerCase().includes(term.toLowerCase())
      }
      renderOption={(categoryId) => <CategoryChip categoryId={categoryId} />}
      onInputChange={() => {
        // 타이핑 할 때마다
        // api 호출 작업 예정
      }}
    />
  );
};

export default CategoryFilter;
