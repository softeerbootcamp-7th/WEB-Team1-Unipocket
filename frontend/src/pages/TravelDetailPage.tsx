import { useState } from 'react';

import BottomSheet from '@/components/layout/BottomSheet';
import ExpenseTable from '@/components/travel-page/ExpenseTable';
import ImportExpenseTable from '@/components/travel-page/ImportExpenseTable';
import TravelDetailHeader from '@/components/travel-page/TravelDetailHeader';

const TravelDetailPage = () => {
  const [isBottomSheetOpen, setBottomSheetOpen] = useState(false);

  return (
    <div className="flex h-full flex-col gap-10 px-4 pt-8 xl:px-30">
      <TravelDetailHeader />

      <div className="bg-background-normal rounded-modal-16 flex-1 rounded-b-none px-2 py-4 shadow">
        <ExpenseTable onOpenBottomSheet={() => setBottomSheetOpen(true)} />
      </div>
      <BottomSheet
        isOpen={isBottomSheetOpen}
        onClose={() => setBottomSheetOpen(false)}
      >
        <ImportExpenseTable />
      </BottomSheet>
    </div>
  );
};

export default TravelDetailPage;
