import { useState } from 'react';

import type { CategoryId } from '@/types/category';

import type { Expense } from '@/api/expenses/type';
import { CASH } from '@/constants/column';

function useSidePanelForm(initialData?: Partial<Expense>) {
  const [prevId, setPrevId] = useState<number | null>(null);
  const [title, setTitle] = useState('');
  const [memo, setMemo] = useState('');
  const [selectedDateTime, setSelectedDateTime] = useState<Date | null>(null);
  const [isDateTimePickerOpen, setIsDateTimePickerOpen] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState<CategoryId | null>(
    null,
  );
  const [selectedCardNumber, setSelectedCardNumber] = useState<string | null>(
    null,
  );
  const [selectedTravelId, setSelectedTravelId] = useState<
    number | string | null
  >(null);

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
    setSelectedCategory(initialData?.category ?? null);
    setSelectedCardNumber(
      initialData?.paymentMethod?.isCash
        ? CASH
        : (initialData?.cardNumber ?? null),
    );
    setSelectedTravelId(initialData?.travel?.travelId ?? null);
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
    selectedCategory,
    setSelectedCategory,
    selectedCardNumber,
    setSelectedCardNumber,
    selectedTravelId,
    setSelectedTravelId,
    resetForm: () => {
      setTitle('');
      setMemo('');
      setSelectedDateTime(null);
      setIsDateTimePickerOpen(false);
      setSelectedCategory(null);
      setSelectedCardNumber(null);
      setSelectedTravelId(null);
    },
  };
}

export default useSidePanelForm;
