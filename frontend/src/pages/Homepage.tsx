import { useState } from 'react';

import WidgetHeader from '@/components/chart/widget/components/WidgetHeader';
import WidgetList from '@/components/chart/widget/components/WidgetList';
import WidgetPicker from '@/components/chart/widget/components/WidgetPicker';
import { useWidgetManager } from '@/components/chart/widget/hook/useWidgetManager';
import { WidgetContext } from '@/components/chart/widget/WidgetContext';
import { DataTable } from '@/components/data-table/DataTable';
import { DataTableFilterProvider } from '@/components/data-table/DataTableFilter';
import DataTableProvider from '@/components/data-table/DataTableProvider';
import TextCellEditor from '@/components/data-table/editors/TextCellEditor';
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

// TODO: 초기 데이터 세팅 (API 연동 전까지 mockData로 대체)
useAccountBookStore.getState().setAccountBook(mockData);

const Homepage = () => {
  const [isExpanded, setIsExpanded] = useState(false);
  const data = getData();

  const widgetManager = useWidgetManager();
  const { isWidgetEditMode } = widgetManager;

  return (
    <WidgetContext.Provider value={widgetManager}>
      <div className="flex min-h-0 flex-1 flex-col gap-5 px-30 pt-8">
        <div className="flex flex-col gap-8">
          <WidgetHeader />
          <WidgetList />
        </div>
        <div className="relative min-h-0 flex-1">
          {isWidgetEditMode && (
            <ExpandableSheet collapsedHeight="100%" isExpandable={false}>
              <WidgetPicker />
            </ExpandableSheet>
          )}
          {!isWidgetEditMode && (
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
                <TextCellEditor />
                <SelectionActionBar />
                <TableSidePanel />
              </DataTableProvider>
            </ExpandableSheet>
          )}
        </div>
      </div>
    </WidgetContext.Provider>
  );
};

export default Homepage;
