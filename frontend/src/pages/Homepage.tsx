import { Suspense, useState } from 'react';

import WidgetHeader from '@/components/chart/widget/components/WidgetHeader';
import WidgetList from '@/components/chart/widget/components/WidgetList';
import WidgetPicker from '@/components/chart/widget/components/WidgetPicker';
import { useWidgetManager } from '@/components/chart/widget/hook/useWidgetManager';
import { WidgetContext } from '@/components/chart/widget/WidgetContext';
import ExpenseTable from '@/components/home-page/ExpenseTable';
import ExpandableSheet from '@/components/layout/ExpandableSheet';
import { Skeleton } from '@/components/ui/skeleton';

const Homepage = () => {
  const [isExpanded, setIsExpanded] = useState(false);

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
          {isWidgetEditMode && (
            <ExpandableSheet collapsedHeight="100%" isExpandable={false}>
              <WidgetPicker
                maxWidgets={maxWidgets}
                availableWidgets={availableWidgets}
                dropZoneProps={pickerDropZone.dropZoneProps}
              />
            </ExpandableSheet>
          )}
          {!isWidgetEditMode && (
            <ExpandableSheet
              isExpanded={isExpanded}
              onToggleExpand={setIsExpanded}
              collapsedHeight="100%"
              expandedHeight="93vh"
            >
              <Suspense fallback={<Skeleton className="h-100 w-full" />}>
                <ExpenseTable />
              </Suspense>
            </ExpandableSheet>
          )}
        </div>
      </div>
    </WidgetContext.Provider>
  );
};

export default Homepage;
