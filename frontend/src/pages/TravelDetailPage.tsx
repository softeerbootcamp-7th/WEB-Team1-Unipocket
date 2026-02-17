import { useState } from 'react';
import { Link } from '@tanstack/react-router';

import Button from '@/components/common/Button';
import Divider from '@/components/common/Divider';
import { DataTable } from '@/components/data-table/DataTable';
import { DataTableFilterProvider } from '@/components/data-table/DataTableFilter';
import DataTableProvider from '@/components/data-table/DataTableProvider';
import CategoryFilter from '@/components/data-table/filters/CategoryFilter';
import DateFilter from '@/components/data-table/filters/DateFilter';
import MerchantFilter from '@/components/data-table/filters/MerchantFilter';
import MethodFilter from '@/components/data-table/filters/MethodFilter';
import SortDropdown from '@/components/data-table/filters/SortDropdown';
import ImportToFolderBar from '@/components/data-table/ImportToFolderBar';
import SelectionActionBar from '@/components/data-table/SelectionActionBar';
import { columns } from '@/components/home-page/columns';
import ExpenseCard from '@/components/home-page/ExpenseCard';
import { type Expense, getData } from '@/components/landing-page/dummy';
import BottomSheet from '@/components/layout/BottomSheet';

import { Icons } from '@/assets';

const TripSummary = () => {
  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-2.5">
        <Link to="/travel">
          <Icons.ChevronBack className="text-label-normal size-6" />
        </Link>
        <span className="heading2-bold text-label-normal">뉴욕 보스턴</span>
      </div>
      <div className="flex gap-1.5">
        <span className="body1-normal-medium text-label-normal">
          2026.01.21 - 2026.01.26
        </span>
        <span className="body1-normal-medium text-label-alternative">
          5박 6일
        </span>
      </div>
    </div>
  );
};

const TravelDetailPage = () => {
  const [isBottomSheetOpen, setBottomSheetOpen] = useState(false);

  const data = getData();
  return (
    <div className="flex h-full flex-col px-30 pt-8">
      <div className="mb-10 flex items-end gap-4">
        <TripSummary />
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

      <div className="bg-background-normal rounded-modal-16 flex-1 rounded-b-none px-2 py-4 shadow">
        <DataTableProvider columns={columns} data={[]}>
          <DataTableFilterProvider>
            <DateFilter />
            <MerchantFilter />
            <CategoryFilter />
            <MethodFilter />
            <div className="flex-1" />
            <SortDropdown />
            <Button
              variant="solid"
              size="md"
              onClick={() => setBottomSheetOpen(true)}
            >
              지출 내역 불러오기
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
          <SelectionActionBar />
        </DataTableProvider>
      </div>
      <BottomSheet
        isOpen={isBottomSheetOpen}
        onClose={() => setBottomSheetOpen(false)}
      >
        <DataTableProvider columns={columns} data={data}>
          <DataTableFilterProvider>
            <DateFilter />
            <MerchantFilter />
            <CategoryFilter />
            <MethodFilter />
            <div className="flex-1" />
            <SortDropdown />
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
          <ImportToFolderBar />
        </DataTableProvider>
      </BottomSheet>
    </div>
  );
};

export default TravelDetailPage;
