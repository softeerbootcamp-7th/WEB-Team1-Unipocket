import Button from '@/components/common/Button';
import { DataTable } from '@/components/common/data-table/DataTable';
import DataTableProvider from '@/components/common/data-table/DataTableProvider';
import CategoryFilter from '@/components/common/data-table/filters/CategoryFilter';
import DateFilter from '@/components/common/data-table/filters/DateFilter';
import MerchantFilter from '@/components/common/data-table/filters/MerchantFilter';
import Divider from '@/components/common/Divider';
import UploadMenu from '@/components/common/upload/UploadMenu';
import { columns } from '@/components/home-page/columns';
import ExpenseCard from '@/components/home-page/ExpenseCard';
import { type Expense, getData } from '@/components/landing-page/dummy';

import { mockData } from '@/stores/mock';
import { useAccountBookStore } from '@/stores/useAccountBookStore';

// import { Icons } from '@/assets';

// TODO: API 연동 전까지 목업 데이터로 사용, 연동 시 해당 부분 삭제 예정
useAccountBookStore.getState().setAccountBook(mockData);

const Homepage = () => {
  const title = useAccountBookStore((state) => state.accountBook?.title);

  const data = getData();
  return (
    <div className="flex flex-col px-30 pt-8">
      <div className="mb-10 flex items-end gap-4">
        <ExpenseCard
          label="총 지출"
          baseCountryCode="KR"
          baseCountryAmount={1402432}
          localCountryCode="US"
          localCountryAmount={12232}
        />
        <Divider style="vertical" className="h-15" />
        <ExpenseCard
          label="이번 달 지출"
          baseCountryCode="KR"
          baseCountryAmount={200342}
          localCountryCode="US"
          localCountryAmount={12232}
        />
        <div className="flex-1" />
        <Button variant="outlined" size="md">
          위젯 편집하기
        </Button>
      </div>
      <div className="border-label-alternative mb-5 flex h-70 items-center justify-center rounded-lg border border-dashed">
        Widget area
        <br />
        {title && `가계부 명: ${title}가계부`}
      </div>
      <div className="bg-background-normal relative rounded-2xl px-2 py-4 shadow">
        {/* <Icons.ChevronBack className="text-label-alternative absolute left-1/2 z-50 size-12 -translate-x-1/2 rotate-90" /> */}
        <DataTableProvider columns={columns} data={data}>
          <DataTableFilterProvider>
            <DateFilter />
            <MerchantFilter />
            <CategoryFilter />
            <div className="flex-1" />
            <UploadMenu />
          </DataTableFilterProvider>
          <DataTable
            groupBy={(row: Expense) =>
              new Date(row.date).toLocaleDateString('ko-KR', {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit',
              })
            }
          />
        </DataTableProvider>
      </div>
    </div>
  );
};

export default Homepage;
