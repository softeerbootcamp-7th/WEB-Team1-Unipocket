import { Suspense, useState } from 'react';

import WidgetList from '@/components/chart/widget/components/WidgetList';
import WidgetPicker from '@/components/chart/widget/components/WidgetPicker';
import { useWidgetManager } from '@/components/chart/widget/hook/useWidgetManager';
import { WidgetContext } from '@/components/chart/widget/WidgetContext';
import ExpenseTable from '@/components/home-page/ExpenseTable';
import HomeHeader from '@/components/home-page/HomeHeader';
import ExpandableSheet from '@/components/layout/ExpandableSheet';
import { Skeleton } from '@/components/ui/skeleton';

import { useRequiredAccountBook } from '@/stores/accountBookStore';

const Homepage = () => {
  const [isExpanded, setIsExpanded] = useState(false);

  const widgetManager = useWidgetManager();
  const { isWidgetEditMode } = widgetManager;
  const accountBook = useRequiredAccountBook();

  return (
    <WidgetContext.Provider value={widgetManager}>
      <div className="flex min-h-0 flex-1 flex-col gap-5 px-4 pt-8 xl:px-30">
        <div className="flex flex-col gap-8">
          <HomeHeader />
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
              <Suspense fallback={<Skeleton className="h-100 w-full" />}>
                <ExpenseTable key={accountBook?.accountBookId} />
              </Suspense>
            </ExpandableSheet>
          )}
        </div>
      </div>
    </WidgetContext.Provider>
  );
};

export default Homepage;
