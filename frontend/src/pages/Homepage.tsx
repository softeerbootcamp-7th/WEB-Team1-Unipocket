import { useState } from 'react';

import WidgetHeader from '@/components/chart/widget/components/WidgetHeader';
import WidgetList from '@/components/chart/widget/components/WidgetList';
import WidgetPicker from '@/components/chart/widget/components/WidgetPicker';
import { useWidgetManager } from '@/components/chart/widget/hook/useWidgetManager';
import { WidgetContext } from '@/components/chart/widget/WidgetContext';
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
  const {
    isWidgetEditMode,
    toggleEditMode,
    maxWidgets,
    displayWidgets,
    availableWidgets,
    handleRemoveWidget,
    listDropZone,
    pickerDropZone,
  } = useWidgetManager();
  return (
    <WidgetContext.Provider value={{ isWidgetEditMode, toggleEditMode }}>
      <div className="flex min-h-0 flex-1 flex-col gap-5 px-30 pt-8">
        <div className="flex flex-col gap-8">
          <WidgetHeader />
          <WidgetList
            displayWidgets={displayWidgets}
            handleRemoveWidget={handleRemoveWidget}
            dropZoneProps={
              isWidgetEditMode ? listDropZone.dropZoneProps : undefined
            }
          />
        </div>
        <div className="relative min-h-0 flex-1">
          <ExpandableSheet
            isExpanded={isExpanded && !isWidgetEditMode}
            onToggleExpand={setIsExpanded}
            collapsedHeight="100%"
            expandedHeight="93vh"
            isExpandable={!isWidgetEditMode}
          >
            {isWidgetEditMode ? (
              <WidgetPicker
                maxWidgets={maxWidgets}
                availableWidgets={availableWidgets}
                dropZoneProps={pickerDropZone.dropZoneProps}
              />
            ) : (
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
            )}
          </ExpandableSheet>
        </div>
      </div>
    </WidgetContext.Provider>
  );
};

export default Homepage;
