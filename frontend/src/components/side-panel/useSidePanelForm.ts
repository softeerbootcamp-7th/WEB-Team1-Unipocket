import { useState } from 'react';

import type { Expense } from '@/api/expenses/type';

function useSidePanelForm(initialData?: Partial<Expense>) {
  const [prevId, setPrevId] = useState<number | null>(null);
  const [title, setTitle] = useState('');
  const [memo, setMemo] = useState('');
  const [selectedDateTime, setSelectedDateTime] = useState<Date | null>(null);
  const [isDateTimePickerOpen, setIsDateTimePickerOpen] = useState(false);

  // 비교하기 전에 미리 null로 변환하여 타입을 완벽히 맞춤
  const currentExpenseId = initialData?.expenseId ?? null;

  if (currentExpenseId !== prevId) {
    setPrevId(currentExpenseId);
    setTitle(initialData?.merchantName || '');
    setMemo(initialData?.memo || '');
    setSelectedDateTime(
      initialData?.occurredAt ? new Date(initialData.occurredAt) : null,
    );
    setIsDateTimePickerOpen(false);
  }

  return {
    title,
    setTitle,
    memo,
    setMemo,
    selectedDateTime,
    setSelectedDateTime,
    isDateTimePickerOpen,
    setIsDateTimePickerOpen,
    resetForm: () => {
      setTitle('');
      setMemo('');
      setSelectedDateTime(null);
      setIsDateTimePickerOpen(false);
    },
  };
}

export default useSidePanelForm;
