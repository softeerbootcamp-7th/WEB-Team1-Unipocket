import { useState } from 'react';

import BottomSheet from '@/components/common/BottomSheet';
import Button from '@/components/common/Button';
import Divider from '@/components/common/Divider';
import { DataTable } from '@/components/data-table/DataTable';
import { DataTableFilterProvider } from '@/components/data-table/DataTableFilter';
import DataTableProvider from '@/components/data-table/DataTableProvider';
import CategoryFilter from '@/components/data-table/filters/CategoryFilter';
import DateFilter from '@/components/data-table/filters/DateFilter';
import MerchantFilter from '@/components/data-table/filters/MerchantFilter';
import MethodFilter from '@/components/data-table/filters/MethodFilter';
import SelectionActionProvider from '@/components/data-table/SelectionActionProvider';
import { columns } from '@/components/home-page/columns';
import ExpenseCard from '@/components/home-page/ExpenseCard';
import { type Expense, getData } from '@/components/landing-page/dummy';

import { useAccountBookStore } from '@/stores/useAccountBookStore';

const TravelDetailPage = () => {
  const title = useAccountBookStore((state) => state.accountBook?.title);
  const [isBottomSheetOpen, setBottomSheetOpen] = useState(false);

  const data = getData();
  return (
    <div className="flex flex-col px-30 pt-8">
      <div className="mb-10 flex items-end gap-4">
        <div className="flex flex-col gap-4">
          <h2 className="heading2-bold text-label-normal">뉴욕 보스턴</h2>
          <div className="flex gap-1.5">
            <span className="body1-normal-medium text-label-normal">
              2026.01.21 - 2026.01.26
            </span>
            <span className="body1-normal-medium text-label-alternative">
              5박 6일
            </span>
          </div>
        </div>
        <Divider style="vertical" className="h-15" />
        <ExpenseCard
          label="총 지출"
          baseCountryCode="KR"
          baseCountryAmount={1402432}
          localCountryCode="US"
          localCountryAmount={12232}
        />
        <div className="flex-1" />
        <Button variant="outlined" size="md">
          위젯 편집하기
        </Button>
      </div>
      {/* <div className="border-label-alternative mb-5 flex h-70 items-center justify-center rounded-lg border border-dashed">
        Widget area
        <br />
        {title && `가계부 명: ${title}가계부`}
      </div> */}
      <div className="bg-background-normal relative rounded-2xl px-2 py-4 shadow">
        {/* <Icons.ChevronBack className="text-label-alternative absolute left-1/2 z-50 size-12 -translate-x-1/2 rotate-90" /> */}
        <DataTableProvider columns={columns} data={[]}>
          <DataTableFilterProvider>
            <DateFilter />
            <MerchantFilter />
            <CategoryFilter />
            <MethodFilter />
            <div className="flex-1" />
            <Button
              variant="solid"
              size="md"
              onClick={() => setBottomSheetOpen(true)}
            >
              지출 내역 추가하기
            </Button>
          </DataTableFilterProvider>
          <DataTable
            groupBy={(row: Expense) =>
              new Date(row.date).toLocaleDateString('ko-KR', {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit',
              })
            }
            blankFallbackText={'여행 지출 내역을 추가해주세요'}
          />
        </DataTableProvider>
      </div>
      <BottomSheet
        isOpen={isBottomSheetOpen}
        onClose={() => setBottomSheetOpen(false)}
      >
        <div className="p-4">Bottom Sheet Content</div>
      </BottomSheet>
    </div>
  );
};

export default TravelDetailPage;
