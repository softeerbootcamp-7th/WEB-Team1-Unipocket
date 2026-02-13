import { useState } from 'react';

import { useRecentSearches } from '@/hooks/useRecentSearches';

import HighlightText from '@/components/data-table/filters/HighlightText';
import RecentSearchList from '@/components/data-table/filters/RecentSearchList';

import { DataTableSearchFilter } from '../DataTableFilter';

// 임시 데이터
const MOCK_MERCHANTS = [
  'Trip.com',
  'Traee',
  'Traveler',
  'Travee',
  'dtryx',
  'Expedia',
  'Airbnb',
  'Coles',
  'Woolworths',
];

const STORAGE_KEY = 'recent_merchant_searches';

const MerchantFilter = () => {
  const [selectedMerchants, setSelectedMerchants] = useState<string[]>([]);
  // 1. 훅 사용으로 로직 대체
  const { recentSearches, addRecentSearch, removeRecentSearch } =
    useRecentSearches(STORAGE_KEY);

  // 선택 토글 헬퍼 함수
  const handleToggle = (keyword: string) => {
    if (selectedMerchants.includes(keyword)) {
      setSelectedMerchants((prev) => prev.filter((item) => item !== keyword));
    } else {
      setSelectedMerchants((prev) => [...prev, keyword]);
    }
  };

  return (
    <DataTableSearchFilter<string>
      title="거래처"
      options={MOCK_MERCHANTS}
      selectedOptions={selectedMerchants}
      setSelectedOptions={setSelectedMerchants}
      // 1. 검색어 변경 감지 및 저장 (선택적이므로 여기서 저장 로직 수행)
      onInputChange={() => {
        // 타이핑 할 때마다
        // api 호출 작업 예정
      }}
      onSelect={(term) => addRecentSearch(term)}
      onSelectMultiple={(terms) => addRecentSearch(terms)}
      // 2. 리스트 아이템 렌더링 (하이라이팅 적용)
      renderOption={(merchant, searchTerm) => (
        <HighlightText text={merchant} highlight={searchTerm} />
      )}
      // 3. 검색어가 없을 때: 최근 검색어 목록 표시
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
