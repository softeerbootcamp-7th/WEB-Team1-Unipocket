import { useState } from 'react';
import { useParams } from '@tanstack/react-router';

import WidgetList from '@/components/chart/widget/components/WidgetList';
import WidgetPicker from '@/components/chart/widget/components/WidgetPicker';
import { useTravelWidgetManager } from '@/components/chart/widget/hook/useTravelWidgetManager';
import { WidgetContext } from '@/components/chart/widget/WidgetContext';
import BottomSheet from '@/components/layout/BottomSheet';
import ExpandableSheet from '@/components/layout/ExpandableSheet';
import ExpenseTable from '@/components/travel-page/ExpenseTable';
import ImportExpenseTable from '@/components/travel-page/ImportExpenseTable';
import TravelDetailHeader from '@/components/travel-page/TravelDetailHeader';

const TravelDetailPage = () => {
  const { travelId } = useParams({ from: '/_app/travel/$travelId' });
  const widgetManager = useTravelWidgetManager(travelId);
  const { isWidgetEditMode } = widgetManager;

  const [isBottomSheetOpen, setBottomSheetOpen] = useState(false);

  return (
    <WidgetContext.Provider value={widgetManager}>
      <div className="flex h-full flex-col gap-10 px-4 pt-8 xl:px-30">
        <TravelDetailHeader />

        <WidgetList />

        <div className="relative min-h-0 flex-1">
          {isWidgetEditMode ? (
            <ExpandableSheet collapsedHeight="100%" isExpandable={false}>
              <WidgetPicker />
            </ExpandableSheet>
          ) : (
            <div className="bg-background-normal rounded-modal-16 flex-1 rounded-b-none px-2 py-4 shadow">
              <ExpenseTable
                onOpenBottomSheet={() => setBottomSheetOpen(true)}
              />
            </div>
          )}
        </div>

        <BottomSheet
          isOpen={isBottomSheetOpen}
          onClose={() => setBottomSheetOpen(false)}
        >
          <ImportExpenseTable />
        </BottomSheet>
      </div>
    </WidgetContext.Provider>
  );
};

export default TravelDetailPage;
