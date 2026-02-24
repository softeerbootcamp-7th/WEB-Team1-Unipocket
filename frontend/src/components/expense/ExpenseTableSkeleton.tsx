import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';

const ExpenseTableSkeleton = () => {
  const headers = [
    '거래처',
    '카테고리',
    '현지 통화',
    '현지 금액',
    '기준 금액',
    '환율',
    '결제 수단',
    '여행',
  ];

  return (
    <div className="flex min-h-0 flex-1 flex-col overflow-hidden">
      {/* 1. 필터 바 영역 스켈레톤 */}
      <div className="mb-4 flex items-center gap-2">
        {/* Date, Merchant, Category, Method 필터 버튼 모양 */}
        <div className="h-8 w-24 animate-pulse rounded bg-gray-200" />
        <div className="h-8 w-24 animate-pulse rounded bg-gray-200" />
        <div className="h-8 w-24 animate-pulse rounded bg-gray-200" />
        <div className="h-8 w-24 animate-pulse rounded bg-gray-200" />
        <div className="flex-1" />
        {/* SortDropdown 버튼 모양 */}
        <div className="h-8 w-24 animate-pulse rounded bg-gray-200" />
      </div>

      {/* 2. 테이블 영역 스켈레톤 */}
      <Table>
        <TableHeader>
          <TableRow>
            {/* 체크박스 열 헤더 */}
            <TableHead className="w-12.5 px-3 py-4">
              <div className="size-4 animate-pulse rounded bg-gray-200" />
            </TableHead>
            {/* 나머지 열 헤더 */}
            {headers.map((header) => (
              <TableHead key={header}>{header}</TableHead>
            ))}
          </TableRow>
        </TableHeader>
        <TableBody>
          {/* 로딩 중에 보여줄 가짜 행(Row) 5개 생성 */}
          {Array.from({ length: 5 }).map((_, rowIndex) => (
            <TableRow key={rowIndex}>
              {/* 체크박스 셀 */}
              <TableCell className="w-12.5 px-3 py-4">
                <div className="size-4 animate-pulse rounded bg-gray-200" />
              </TableCell>
              {/* 나머지 데이터 셀들 */}
              {headers.map((_, colIndex) => (
                <TableCell key={colIndex} className="px-3 py-4">
                  {/* 랜덤한 너비의 텍스트 스켈레톤 (자연스러움을 위해) */}
                  <div
                    className="h-4 animate-pulse rounded bg-gray-200"
                    style={{
                      width: `${Math.floor(Math.random() * 40) + 40}%`, // 40% ~ 80% 너비
                    }}
                  />
                </TableCell>
              ))}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
};

export default ExpenseTableSkeleton;
