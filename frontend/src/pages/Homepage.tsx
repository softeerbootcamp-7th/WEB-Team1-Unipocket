import { useState } from 'react';

import WidgetSection from '@/components/chart/widget/components/WidgetSection';
import { DataTable } from '@/components/data-table/DataTable';
import DataTableCellEditor from '@/components/data-table/DataTableCellEditor';
import { DataTableFilterProvider } from '@/components/data-table/DataTableFilter';
import DataTableProvider from '@/components/data-table/DataTableProvider';
import CategoryFilter from '@/components/data-table/filters/CategoryFilter';
import DateFilter from '@/components/data-table/filters/DateFilter';
import MerchantFilter from '@/components/data-table/filters/MerchantFilter';
import MethodFilter from '@/components/data-table/filters/MethodFilter';
import SortDropdown from '@/components/data-table/filters/SortDropdown';
import SelectionActionBar from '@/components/data-table/SelectionActionBar';
import { columns } from '@/components/home-page/columns';
import { type Expense, getData } from '@/components/landing-page/dummy';
import ExpandableSheet from '@/components/layout/ExpandableSheet';
import TableSidePanel from '@/components/side-panel/TableSidePanel';
import UploadMenu from '@/components/upload/UploadMenu';

import { mockData } from '@/stores/mock';
import { useAccountBookStore } from '@/stores/useAccountBookStore';

useAccountBookStore.getState().setAccountBook(mockData);

const Homepage = () => {
  const [isExpanded, setIsExpanded] = useState(false);
  const data = getData();
  return (
    <div className="flex min-h-0 flex-1 flex-col gap-5 px-30 pt-8">
      <WidgetSection />
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
            <SelectionActionBar />
            <TableSidePanel />
          </DataTableProvider>
        </ExpandableSheet>
      </div>
    </div>
  );
};

export default Homepage;
