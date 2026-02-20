import { formatDateTime } from '@/components/calendar/date.utils';
import { CategoryChip } from '@/components/common/Chip';
import PaymentMethodDisplay from '@/components/expense/PaymentMethodDisplay';
import { EmptyValue } from '@/components/side-panel/EmptyValue';
import type { ValueItemProps } from '@/components/side-panel/ValueContainer';

import type { Expense } from '@/api/expenses/type';

interface UseSidePanelValuesParams {
  initialData?: Partial<Expense>;
  selectedDateTime: Date | null;
  onDateTimeClick: () => void;
}

export function useSidePanelValues({
  initialData,
  selectedDateTime,
  onDateTimeClick,
}: UseSidePanelValuesParams): ValueItemProps[] {
  const categoryValue = initialData?.category ? (
    <CategoryChip categoryId={initialData.category} />
  ) : (
    <EmptyValue />
  );

  const paymentValue = initialData?.paymentMethod ? (
    initialData.paymentMethod.isCash ? (
      '현금'
    ) : (
      <PaymentMethodDisplay paymentMethod={initialData.paymentMethod} />
    )
  ) : (
    <EmptyValue />
  );

  return [
    {
      label: '일시',
      value: selectedDateTime ? (
        formatDateTime(selectedDateTime)
      ) : (
        <EmptyValue />
      ),
      onClick: onDateTimeClick,
    },
    { label: '카테고리', value: categoryValue },
    { label: '결제 수단', value: paymentValue },
    { label: '여행', value: initialData?.travel?.name ?? <EmptyValue /> },
  ];
}
