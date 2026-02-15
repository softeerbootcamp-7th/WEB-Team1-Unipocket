import { useState } from 'react';

import { MOCK_WIDGET_DATA } from '@/components/chart/widget/mock';
import { renderWidget } from '@/components/chart/widget/renderWidget';
import Button from '@/components/common/Button';
import Divider from '@/components/common/Divider';
import { DataTable } from '@/components/data-table/DataTable';
import DataTableCellEditor from '@/components/data-table/DataTableCellEditor';
import { DataTableFilterProvider } from '@/components/data-table/DataTableFilter';
import DataTableProvider from '@/components/data-table/DataTableProvider';
import CategoryFilter from '@/components/data-table/filters/CategoryFilter';
import DateFilter from '@/components/data-table/filters/DateFilter';
import MerchantFilter from '@/components/data-table/filters/MerchantFilter';
import MethodFilter from '@/components/data-table/filters/MethodFilter';
import SortDropdown from '@/components/data-table/filters/SortDropdown';
import SelectionActionProvider from '@/components/data-table/SelectionActionProvider';
import { columns } from '@/components/home-page/columns';
import ExpenseCard from '@/components/home-page/ExpenseCard';
import { type Expense, getData } from '@/components/landing-page/dummy';
import ExpandableSheet from '@/components/layout/menu/ExpandableSheet';
import SidePanel from '@/components/side-panel/SidePanel';
import UploadMenu from '@/components/upload/UploadMenu';

import { mockData } from '@/stores/mock';
import { useAccountBookStore } from '@/stores/useAccountBookStore';

useAccountBookStore.getState().setAccountBook(mockData);

const Homepage = () => {
  const [isExpanded, setIsExpanded] = useState(false);
  const data = getData();
  return (
    <div className="flex min-h-0 flex-1 flex-col gap-5 px-30 pt-8">
      {/* widget section */}
      <div className="flex flex-none flex-col gap-10">
        <div className="flex items-end gap-4">
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
        <div className="flex w-full justify-center gap-8">
          {MOCK_WIDGET_DATA.map((widget) => (
            <div key={widget.order}>{renderWidget(widget)}</div>
          ))}
        </div>
      </div>
      <div className="relative min-h-0 flex-1">
        <ExpandableSheet
          isExpanded={isExpanded}
          onToggleExpand={setIsExpanded}
          collapsedHeight="100%"
          expandedHeight="93vh"
        >
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
            <DataTableCellEditor />
            <SelectionActionProvider />
            <SidePanel />
          </DataTableProvider>
        </ExpandableSheet>
      </div>
    </div>
  );
};

export default Homepage;
