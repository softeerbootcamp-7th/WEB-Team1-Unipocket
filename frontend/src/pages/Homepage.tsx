import WidgetSection from '@/components/chart/widget/WidgetSection';
import { DataTable } from '@/components/data-table/DataTable';
import { DataTableFilterProvider } from '@/components/data-table/DataTableFilter';
import DataTableProvider from '@/components/data-table/DataTableProvider';
import CategoryFilter from '@/components/data-table/filters/CategoryFilter';
import DateFilter from '@/components/data-table/filters/DateFilter';
import MerchantFilter from '@/components/data-table/filters/MerchantFilter';
import SortDropdown from '@/components/data-table/filters/SortDropdown';
import MethodFilter from '@/components/data-table/filters/MethodFilter';
import { columns } from '@/components/home-page/columns';
import { type Expense, getData } from '@/components/landing-page/dummy';
import UploadMenu from '@/components/upload/UploadMenu';

import { mockData } from '@/stores/mock';
import { useAccountBookStore } from '@/stores/useAccountBookStore';

// import { Icons } from '@/assets';

// TODO: API 연동 전까지 목업 데이터로 사용, 연동 시 해당 부분 삭제 예정
useAccountBookStore.getState().setAccountBook(mockData);

const Homepage = () => {
  const data = getData();
  return (
    <div className="flex flex-col gap-5 px-30 pt-8">
      <WidgetSection />
      <div className="bg-background-normal shadow-semantic-subtle relative rounded-2xl px-2 py-4">
        {/* <Icons.ChevronBack className="text-label-alternative absolute left-1/2 z-50 size-12 -translate-x-1/2 rotate-90" /> */}
        <DataTableProvider columns={columns} data={data}>
          <DataTableFilterProvider>
            <DateFilter />
            <MerchantFilter />
            <CategoryFilter />
            <MethodFilter />
            <div className="flex-1" />
            <SortDropdown />
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
