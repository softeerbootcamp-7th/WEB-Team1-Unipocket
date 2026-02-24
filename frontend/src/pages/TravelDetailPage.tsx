import { Suspense, useState } from 'react';
import { Link } from '@tanstack/react-router';

import Button from '@/components/common/Button';
import Divider from '@/components/common/Divider';
import ExpenseTableSkeleton from '@/components/expense/ExpenseTableSkeleton';
import ExpenseCard from '@/components/home-page/ExpenseCard';
import BottomSheet from '@/components/layout/BottomSheet';
import ExpenseTable from '@/components/travel-page/ExpenseTable';
import ImportExpenseTable from '@/components/travel-page/ImportExpenseTable';

import { Icons } from '@/assets';

const TripSummary = () => {
  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-2.5">
        <Link to="/travel">
          <Icons.ChevronBack className="text-label-normal size-6" />
        </Link>
        <span className="heading2-bold text-label-normal">뉴욕 보스턴</span>
      </div>
      <div className="flex gap-1.5">
        <span className="body1-normal-medium text-label-normal">
          2026.01.21 - 2026.01.26
        </span>
        <span className="body1-normal-medium text-label-alternative">
          5박 6일
        </span>
      </div>
    </div>
  );
};

const TravelDetailPage = () => {
  const [isBottomSheetOpen, setBottomSheetOpen] = useState(false);

  return (
    <div className="flex h-full flex-col px-4 pt-8 xl:px-30">
      <div className="mb-10 flex items-end gap-4">
        <TripSummary />
        <Divider style="vertical" className="h-15" />
        <ExpenseCard
          label="총 지출"
          baseCountryCode="KR"
          baseCountryAmount={1402432}
          localCountryCode="US"
          localCountryAmount={12232}
        />
        <div className="flex-1" />
        <Button variant="outlined" size="md">
          위젯 편집하기
        </Button>
      </div>

      <div className="bg-background-normal rounded-modal-16 flex-1 rounded-b-none px-2 py-4 shadow">
        <ExpenseTable onOpenBottomSheet={() => setBottomSheetOpen(true)} />
      </div>
      <BottomSheet
        isOpen={isBottomSheetOpen}
        onClose={() => setBottomSheetOpen(false)}
      >
        <Suspense fallback={<ExpenseTableSkeleton />}>
          <ImportExpenseTable />
        </Suspense>
      </BottomSheet>
    </div>
  );
};

export default TravelDetailPage;
