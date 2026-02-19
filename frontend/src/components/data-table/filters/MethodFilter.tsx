import { useState } from 'react';

import { useRecentSearches } from '@/hooks/useRecentSearches';

import HighlightText from '@/components/data-table/filters/HighlightText';
import RecentSearchList from '@/components/data-table/filters/RecentSearchList';

import { DataTableSearchFilter } from '../DataTableFilter';

// 임시 데이터
const MOCK_METHODS = [
  '토스뱅크 오렌지밀크',
  'Commonwealth Debit MasterCard',
  '현금',
  '신한 Deep Dream',
  '현대카드 M BOOST',
  '국민 체크카드',
  '삼성 iD ON',
  '하나 트래블로그',
  '우리 NU 오하쳌',
  '네이버페이',
  '카카오페이',
  '삼성페이',
  'Apple Pay',
  '페이코',
  'PayPal',
  'Wise Debit',
  '계좌이체',
  '문화상품권',
  '비트코인',
];

const STORAGE_KEY = 'recent_method_searches';

const MethodFilter = () => {
  const [selectedMethods, setSelectedMethods] = useState<string[]>([]);

  const { recentSearches, addRecentSearch, removeRecentSearch } =
    useRecentSearches(STORAGE_KEY);

  const handleToggle = (keyword: string) => {
    if (selectedMethods.includes(keyword)) {
      setSelectedMethods((prev) => prev.filter((item) => item !== keyword));
    } else {
      setSelectedMethods((prev) => [...prev, keyword]);
    }
  };

  return (
    <DataTableSearchFilter<string>
      title="결제수단"
      options={MOCK_METHODS}
      selectedOptions={selectedMethods}
      setSelectedOptions={setSelectedMethods}
      // 1. 검색어 변경 감지 및 저장 (선택적이므로 여기서 저장 로직 수행)
      onInputChange={() => {
        // 타이핑 할 때마다
        // api 호출 작업 예정
      }}
      onSelect={(term) => addRecentSearch(term)}
      // 2. 리스트 아이템 렌더링 (하이라이팅 적용)
      renderOption={(method, searchTerm) => (
        <HighlightText text={method} highlight={searchTerm} />
      )}
      // 3. 검색어가 없을 때: 최근 검색어 목록 표시
      renderEmptyState={() => (
        <RecentSearchList
          title="최근 검색어"
          searches={recentSearches}
          selectedItems={selectedMethods}
          onToggle={handleToggle}
          onRemove={removeRecentSearch}
        />
      )}
    />
  );
};

export default MethodFilter;
