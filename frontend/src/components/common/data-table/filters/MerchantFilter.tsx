import { useState } from 'react';
import { X } from 'lucide-react';

import { Checkbox } from '@/components/ui/checkbox';

import Tag from '../../Tag';
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

interface HighlightTextProps {
  text: string;
  highlight: string;
}

const HighlightText = ({ text, highlight }: HighlightTextProps) => {
  if (!highlight.trim()) return <span className="caption1-medium">{text}</span>;
  const regex = new RegExp(`(${highlight})`, 'gi');
  return (
    <span className="caption1-medium">
      {text.split(regex).map((part, i) =>
        regex.test(part) ? (
          <span key={i} className="underline">
            {part}
          </span>
        ) : (
          <span key={i}>{part}</span>
        ),
      )}
    </span>
  );
};

const MerchantFilter = () => {
  const [selectedMerchants, setSelectedMerchants] = useState<string[]>([]);
  const [recentSearches, setRecentSearches] = useState<string[]>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      return saved ? JSON.parse(saved) : [];
    } catch {
      return [];
    }
  });

  // 검색어 저장
  const saveSearchTerm = (term: string) => {
    if (!term.trim()) return;
    const updated = [term, ...recentSearches.filter((t) => t !== term)].slice(
      0,
      5,
    );
    setRecentSearches(updated);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(updated));
  };

  // 최근 검색어 삭제
  const removeRecentSearch = (e: React.MouseEvent, keyword: string) => {
    e.stopPropagation(); // 부모(체크박스/선택 등) 이벤트 방지
    const updated = recentSearches.filter((item) => item !== keyword);
    setRecentSearches(updated);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(updated));
  };

  return (
    <DataTableSearchFilter<string>
      title="거래처"
      options={MOCK_MERCHANTS}
      selectedOptions={selectedMerchants}
      setSelectedOptions={setSelectedMerchants}
      // 1. 검색어 변경 감지 및 저장 (선택적이므로 여기서 저장 로직 수행)
      onSearch={() => {
        // 타이핑 할 때마다
        // api 호출 작업 예정
      }}
      renderTag={(merchant, onRemove) => {
        return <Tag type={merchant} onRemove={onRemove} />;
      }}
      // 2. 리스트 아이템 렌더링 (하이라이팅 적용)
      renderOption={(merchant, searchTerm) => (
        <HighlightText text={merchant} highlight={searchTerm} />
      )}
      // 3. 검색어가 없을 때: 최근 검색어 목록 표시
      renderEmptyState={() => (
        <div className="caption2-bold min-h-33">
          {recentSearches.length > 0 && (
            <>
              <span className="text-label-alternative">최근 검색어</span>
              {recentSearches.map((keyword) => (
                <div
                  key={keyword}
                  onClick={() => {
                    if (selectedMerchants.includes(keyword)) {
                      setSelectedMerchants(
                        selectedMerchants.filter((item) => item !== keyword),
                      );
                    } else {
                      setSelectedMerchants([...selectedMerchants, keyword]);
                    }
                  }}
                  className="group mt-2.5 flex cursor-pointer items-center gap-2.5 p-1"
                >
                  <Checkbox checked={selectedMerchants.includes(keyword)} />
                  <span className="text-label-neutral caption1-medium pt-0.5">
                    {keyword}
                  </span>
                  <button
                    onClick={(e) => removeRecentSearch(e, keyword)}
                    className="ml-auto opacity-0 transition group-hover:opacity-100"
                  >
                    <X size={14} />
                  </button>
                </div>
              ))}
            </>
          )}
        </div>
      )}
      renderFooter={(searchTerm) => (
        <button
          className="caption1-bold text-primary-normal mt-2.5 cursor-pointer truncate text-start"
          onClick={() => {
            saveSearchTerm(searchTerm); // 버튼 클릭 시 검색어 저장
          }}
        >
          '{searchTerm}'(이)가 포함된 모든 내역 필터링
        </button>
      )}
    />
  );
};

export default MerchantFilter;
