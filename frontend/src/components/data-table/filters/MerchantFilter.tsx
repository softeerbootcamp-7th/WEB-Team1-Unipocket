import { useState } from 'react';

import { useRecentSearches } from '@/hooks/useRecentSearches';

import { useDataTableFilter } from '@/components/data-table/context';
import HighlightText from '@/components/data-table/filters/HighlightText';
import RecentSearchList from '@/components/data-table/filters/RecentSearchList';

import { useSearchMerchantNamesQuery } from '@/api/expenses/query';
import { useRequiredAccountBook } from '@/stores/accountBookStore';

import { DataTableSearchFilter } from '../DataTableFilter';

const STORAGE_KEY = 'recent_merchant_searches';

const MerchantFilter = () => {
  const { accountBookId } = useRequiredAccountBook();
  const { filter, updateFilter } = useDataTableFilter();

  const [searchTerm, setSearchTerm] = useState('');

  const { data } = useSearchMerchantNamesQuery(accountBookId, searchTerm);

  const merchantOptions = data?.merchantNames || [];

  const selectedMerchants = filter.merchantName || [];

  const { recentSearches, addRecentSearch, removeRecentSearch } =
    useRecentSearches(STORAGE_KEY);

  const handleMerchantChange = (selected: string[]) => {
    updateFilter({
      merchantName: selected.length > 0 ? selected : undefined,
    });
  };

  const handleToggle = (keyword: string) => {
    if (selectedMerchants.includes(keyword)) {
      handleMerchantChange(
        selectedMerchants.filter((item) => item !== keyword),
      );
    } else {
      handleMerchantChange([...selectedMerchants, keyword]);
    }
  };

  return (
    <DataTableSearchFilter<string>
      title="거래처"
      options={merchantOptions}
      selectedOptions={selectedMerchants}
      setSelectedOptions={handleMerchantChange}
      onInputChange={(term) => setSearchTerm(term)}
      onSelect={(term) => addRecentSearch(term)}
      onSelectMultiple={(terms) => addRecentSearch(terms)}
      renderOption={(merchant, searchTerm) => (
        <HighlightText text={merchant} highlight={searchTerm} />
      )}
      renderEmptyState={() => (
        <RecentSearchList
          title="최근 검색어"
          searches={recentSearches}
          selectedItems={selectedMerchants}
          onToggle={handleToggle}
          onRemove={removeRecentSearch}
        />
      )}
      renderSearchAllTrigger={(searchTerm, onSelectAll) => (
        <button
          onClick={onSelectAll}
          className="caption1-bold text-primary-normal mt-2.5 cursor-pointer truncate text-start"
        >
          '{searchTerm}'(이)가 포함된 모든 내역 필터링
        </button>
      )}
    />
  );
};

export default MerchantFilter;
